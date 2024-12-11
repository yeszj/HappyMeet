package cn.yanhu.baselib.view.captchalsyout;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.ThreadUtils;

import java.util.ArrayList;
import java.util.List;

import cn.yanhu.baselib.R;

public class VerifyCodeNumberLayout extends RelativeLayout {
    private Context context;
    List<MyEditText> editViews;
    private int textColor;
    private int codeNumber;
    private LinearLayout ll_content;

    public VerifyCodeNumberLayout(Context context) {
        this(context, null);
    }

    public VerifyCodeNumberLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerifyCodeNumberLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        loadView(attrs);
    }

    private void loadView(AttributeSet attrs) {
        View view = LayoutInflater.from(context).inflate(R.layout.verification_code, this);
        ll_content = view.findViewById(R.id.ll_code_content);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Verificationcode);
        textColor = typedArray.getColor(R.styleable.Verificationcode_code_text_color, getResources().getColor(R.color.white));
        codeNumber = typedArray.getInt(R.styleable.Verificationcode_code_number, 16);
        if (codeNumber > 8 && codeNumber % 2 == 1) codeNumber += 1;
        initView();
    }

    private void initView() {
        editViews = new ArrayList<>();
        LinearLayout linearLayout1 = new LinearLayout(context);
        LinearLayout linearLayout2 = new LinearLayout(context);
        for (int i = 0; i < codeNumber; i++) {
            LinearLayout.LayoutParams layout_param = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            View item_view = LayoutInflater.from(context).inflate(R.layout.verifation_code_item, null);
            final MyEditText code_child = item_view.findViewById(R.id.tv_code);
            code_child.setTextColor(textColor);
            code_child.setBackground(ContextCompat.getDrawable(context,R.drawable.bg_text_normal));
            code_child.setId(i);
            code_child.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    if (b == true) {
                        code_child.setBackground(ContextCompat.getDrawable(context,R.drawable.bg_text_focus));
                    } else {
                        code_child.setBackground(ContextCompat.getDrawable(context,R.drawable.bg_text_normal));
                    }
                }
            });
            code_child.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (editable != null && editable.length() > 0) {
                        String inputcontent = editable.toString();
                        int location = code_child.getId();
                        if (location + inputcontent.length() <= codeNumber) {
                            if (inputcontent.length() > 1 && location < codeNumber - 1) {
                                for (int i = location; i < editViews.size(); i++) {
                                    MyEditText myEditText = editViews.get(i);
                                    myEditText.setText("");
                                }
                                for (int i = 0; i < inputcontent.length(); i++) {
                                    showCode(i + location, inputcontent.charAt(i) + "");
                                }
                                editViews.get(location + inputcontent.length() - 1).setSelection(1);
                            } else {
                                if (location < codeNumber - 1) {
                                    showCode(location + 1, "");
                                    code_child.setBackground(ContextCompat.getDrawable(context,R.drawable.bg_text_complete));
                                } else {
                                    code_child.setBackground(ContextCompat.getDrawable(context,R.drawable.bg_text_complete));
                                    String content = "";
                                    for (int i = 0; i < codeNumber; i++) {
                                        content += editViews.get(i).getText();
                                    }
                                    if (onInputListener != null)
                                        onInputListener.onSucess(content);
                                }
                            }
                        } else {
                            code_child.setText("");
                            Toast.makeText(context, "长度超过" + codeNumber + "，请检查", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
            // 监听验证码删除按键
            code_child.setOnKeyListener((view, keyCode, keyEvent) -> {
                if (keyCode == KeyEvent.KEYCODE_DEL && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    int location = code_child.getId();
                    if (code_child.getText().toString().equals("")) {
                        if (location >= 1)
                            removeCode(location - 1);
                        return true;
                    } else
                        return false;
                }
                return false;
            });
            code_child.setZTListener((id, content) -> {
                if (content.length() > codeNumber) {
                    Toast.makeText(context, "长度超过" + codeNumber + "，请检查", Toast.LENGTH_SHORT).show();
                } else if (content.length() > 0) {
                    for (int i1 = 0; i1 < editViews.size(); i1++) {
                        MyEditText myEditText = editViews.get(i1);
                        myEditText.setText("");
                    }
                    showCode(0, "");
                    for (int i1 = 0; i1 < content.length(); i1++) {
                        showCode(i1, content.charAt(i1) + "");
                    }
                    editViews.get(content.length() - 1).setSelection(1);
                }
                return false;
            });
            if (codeNumber <= 8) {
                linearLayout1.addView(item_view, layout_param);
            } else {
                if (i >= codeNumber / 2) {
                    linearLayout2.addView(item_view, layout_param);
                } else
                    linearLayout1.addView(item_view, layout_param);
            }
            editViews.add(code_child);
        }
        if (codeNumber <= 8) {
            ll_content.addView(linearLayout1);
        } else {
            ll_content.addView(linearLayout1);
            ll_content.addView(linearLayout2);
        }
        InputFilter[] filters = {new InputFilter.LengthFilter(1)};
        editViews.get(codeNumber - 1).setFilters(filters);
        editViews.get(0).setFocusable(true);
        editViews.get(0).setFocusableInTouchMode(true);
        editViews.get(0).requestFocus();
    }

    private void showCode(int location, String code) {
        EditText et_code = editViews.get(location);
        et_code.setFocusable(true);
        et_code.setFocusableInTouchMode(true);
        et_code.requestFocus();
        et_code.setText(code);
    }

    public void showAllCode(String password){
        char[] chars = password.toCharArray();
        for (int i =0;i<chars.length;i++){
            showCode(i,chars[i]+"");
        }
    }


    private void removeCode(int location) {
        EditText et_code = editViews.get(location);
        et_code.setFocusable(true);
        et_code.setFocusableInTouchMode(true);
        et_code.requestFocus();
        et_code.setText("");
    }

    public void removeAll(){
        for (MyEditText editView : editViews) {
            editView.setText("");
            editView.setBackground(ContextCompat.getDrawable(context,R.drawable.bg_text_normal));
        }
        MyEditText myEditText = editViews.get(0);
        myEditText.setFocusable(true);
        myEditText.setFocusableInTouchMode(true);
        myEditText.requestFocus();
    }

    public void showSoftInput(){
        MyEditText myEditText = editViews.get(0);
        myEditText.setFocusable(true);
        myEditText.setFocusableInTouchMode(true);
        myEditText.requestFocus();
        ThreadUtils.getMainHandler().postDelayed(KeyboardUtils::showSoftInput,100);

    }

    private OnInputListener onInputListener;

    //定义回调
    public interface OnInputListener {
        void onSucess(String code);
    }

    public void setOnInputListener(OnInputListener onInputListener) {
        this.onInputListener = onInputListener;
    }
}
