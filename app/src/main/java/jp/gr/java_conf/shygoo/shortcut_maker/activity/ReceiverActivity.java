package jp.gr.java_conf.shygoo.shortcut_maker.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import icepick.Icepick;
import icepick.State;
import jp.gr.java_conf.shygoo.shortcut_maker.R;
import jp.gr.java_conf.shygoo.shortcut_maker.util.ComponentUtil;
import jp.gr.java_conf.shygoo.shortcut_maker.util.SharedPreferencesUtil;

public class ReceiverActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_ACTIVITY = 100;

    @BindView(R.id.shortcut_icon)
    AppCompatImageView shortcutIcon;

    @BindView(R.id.shortcut_uri)
    TextView shortcutUri;

    @BindView(R.id.shortcut_name)
    EditText shortcutName;

    @BindView(R.id.shortcut_type)
    RadioGroup shortcutType;

    @BindView(R.id.shortcut_component)
    TextView shortcutComponent;

    @State
    Intent viewIntent;

    @State
    ComponentName component;

    @State
    @DrawableRes
    int iconRes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver);
        ButterKnife.bind(this);
        if (savedInstanceState == null) {
            onReceive(getIntent());
        } else {
            Icepick.restoreInstanceState(this, savedInstanceState);
        }
    }

    @Override
    protected void onNewIntent(Intent newIntent) {
        super.onNewIntent(newIntent);
        setIntent(newIntent);
        onReceive(newIntent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    private void onReceive(Intent intent) {
        Log.d("intent", intent.toString());
        String action = intent.getAction();
        String type = intent.getType();
        if (type == null) {
            return;
        }
        switch (action) {
            case Intent.ACTION_SEND:
                if ("text/plain".equals(type)) {
                    handleSendText(intent);
                } else if (type.startsWith("image/")) {
                    handleSendImage(intent);
                }
                break;
        }
    }

    private void handleSendText(Intent sendIntent) {
        String sharedText = sendIntent.getStringExtra(Intent.EXTRA_TEXT);
        TextView dummyText = new TextView(this);
        dummyText.setText(sharedText);
        Linkify.addLinks(dummyText, Linkify.WEB_URLS);
        URLSpan[] urls = dummyText.getUrls();
        if (urls == null || urls.length == 0) {
            finish(R.string.error_not_supported);
            return;
        }
        Uri webpage = Uri.parse(urls[0].getURL());

        iconRes = R.drawable.ic_bookmark;
        component = SharedPreferencesUtil.loadDefaultComponent(this, sendIntent.getType());

        viewIntent = new Intent(Intent.ACTION_VIEW, webpage);

        shortcutIcon.setImageResource(iconRes);
        shortcutUri.setText(webpage.toString());
        shortcutName.setText(sendIntent.getStringExtra(Intent.EXTRA_SUBJECT));
        setComponent(component);
    }

    private void handleSendImage(Intent sendIntent) {

        Uri imageUri = sendIntent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri == null) {
            finish(R.string.error_not_supported);
            return;
        }

        iconRes = R.drawable.ic_image;
        String type = sendIntent.getType();
        component = SharedPreferencesUtil.loadDefaultComponent(this, type);

        viewIntent = new Intent(Intent.ACTION_VIEW);
        viewIntent.setDataAndType(imageUri, type);

        shortcutIcon.setImageResource(iconRes);
        shortcutUri.setText(imageUri.toString());
        shortcutName.setText(imageUri.getLastPathSegment());
        setComponent(component);
    }

    @OnClick({R.id.type_specified, R.id.type_unspecified})
    void onShortcutTypeClicked(RadioButton button) {
        switch (button.getId()) {
            case R.id.type_specified:
                requestToSelectComponent();
                break;
            case R.id.type_unspecified:
                setComponent(null);
                break;
        }
    }

    private void requestToSelectComponent() {
        Intent intentPick = new Intent();
        intentPick.setAction(Intent.ACTION_PICK_ACTIVITY);
        intentPick.putExtra(Intent.EXTRA_TITLE, R.string.label_launch_by);
        intentPick.putExtra(Intent.EXTRA_INTENT, viewIntent);
        this.startActivityForResult(intentPick, REQUEST_CODE_PICK_ACTIVITY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_CODE_PICK_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    setComponent(data.getComponent());
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private void setComponent(ComponentName component) {
        this.component = component;
        if (component == null) {
            shortcutType.check(R.id.type_unspecified);
            shortcutComponent.setText(null);
        } else {
            shortcutType.check(R.id.type_specified);
            shortcutComponent.setText(ComponentUtil.getName(this, component));
        }
    }

    @OnClick(R.id.button_create)
    void createShortcut() {

        Intent shortcutIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        if (component != null) {
            viewIntent.setComponent(component);
        }
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, viewIntent);
        String name = shortcutName.getText().toString();
        if (TextUtils.isEmpty(name)) {
            name = getString(R.string.name_default);
        }
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
        Parcelable icon = Intent.ShortcutIconResource.fromContext(this, iconRes);
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
        sendBroadcast(shortcutIntent);

        SharedPreferencesUtil.saveDefaultComponent(this, getIntent().getType(), component);

        finish(R.string.message_succeeded);
    }

    @OnClick(R.id.button_cancel)
    void cancel() {
        finish();
    }

    private void finish(@StringRes int errorMessage) {
        Toast.makeText(this, getText(errorMessage), Toast.LENGTH_SHORT).show();
        finish();
    }
}
