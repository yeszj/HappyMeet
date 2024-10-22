package cn.yanhu.imchat.custom.message;

import android.content.Context;
import android.view.View;

import com.hyphenate.chat.EMMessage;


/**
 * @author: zhengjun
 * created: 2024/1/19
 * desc:
 */
public class BaseEaseChatRowFile extends BaseEaseChatRow {
    public BaseEaseChatRowFile(Context context, boolean isSender) {
        super(context, isSender);
    }

    public BaseEaseChatRowFile(Context context, EMMessage message, int position, Object adapter) {
        super(context, message, position, adapter);
    }

    @Override
    protected void onInflateView() {
        super.onInflateView();

    }

    @Override
    protected void onFindViewById() {
        super.onFindViewById();
    }

    @Override
    protected void onSetUpView() {
        super.onSetUpView();
    }

    @Override
    protected void onMessageCreate() {
        super.onMessageCreate();
        if (progressBar!=null){
            //progressBar.setVisibility(View.VISIBLE);
        }
        if (percentageView != null)
            percentageView.setVisibility(View.INVISIBLE);
        if (statusView != null)
            statusView.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onMessageSuccess() {
        super.onMessageSuccess();
        if (progressBar!=null){
            progressBar.setVisibility(View.INVISIBLE);
        }
        if (percentageView != null)
            percentageView.setVisibility(View.INVISIBLE);
        if (statusView != null)
            statusView.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onMessageError() {
        super.onMessageError();
        if (progressBar!=null){
            progressBar.setVisibility(View.INVISIBLE);
        }
        if (percentageView != null)
            percentageView.setVisibility(View.INVISIBLE);
        if (statusView != null)
            statusView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onMessageInProgress() {
        super.onMessageInProgress();
        if(progressBar!=null && progressBar.getVisibility() != VISIBLE) {
           // progressBar.setVisibility(View.VISIBLE);
        }
        if (percentageView != null) {
            percentageView.setVisibility(View.VISIBLE);
            percentageView.setText(message.progress() + "%");
        }
        if (statusView != null)
            statusView.setVisibility(View.INVISIBLE);
    }
}
