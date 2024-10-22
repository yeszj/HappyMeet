package com.pcl.sdklib.sdk.baiduFace;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.pcl.sdklib.R;

public class VerifyFailureActivity extends FragmentActivity {
    private static final String TAG = VerifyFailureActivity.class.getSimpleName();

    private TextView mTextErrMessage;
    private TextView mTextErrTips;
    private Button mBtnReturnHome;
    private Button mBtnReCollect;
    private ImageView mImageError;
    /**
     * 认证状态，取值如下：
     * 0 ： 正常
     * 1 ： 身份证号与姓名不匹配或该身份证号不存在
     * 2 ： 公安网图片不存在或质量过低
     */
    private int verifyStatus;
    /**
     * 判断设备是否发生过风险行为来判断风险级别，取值（数值由高到低）：
     * 1 ：高危、2 ：嫌疑、3 ：普通、4 ：正常
     */
    private int riskLevel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_failure);
        initView();
        initData();
    }


    private void initView() {
        mTextErrMessage = (TextView) findViewById(R.id.text_err_message);
        mTextErrTips = (TextView) findViewById(R.id.text_err_tips);
        mBtnReturnHome = (Button) findViewById(R.id.btn_return_home);
        mBtnReCollect = (Button) findViewById(R.id.btn_recollect);
        mImageError = (ImageView) findViewById(R.id.image_failure_icon);
    }

    public void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            int errCode = intent.getIntExtra("err_code", 0);
            verifyStatus = intent.getIntExtra("verify_status", 0);
            riskLevel = intent.getIntExtra("risk_level", 0);
            judgeErrorCode(errCode);
        }
    }

    private void judgeErrorCode(int errCode) {
        Log.e(TAG, "errCode = " + errCode + ", verifyStatus = " + verifyStatus + ", riskLevel = " + riskLevel);
        // 设备风险级别 1:高危、2:嫌疑、3:普通、4:正常
        if (riskLevel == 1 || riskLevel == 2) {
            mTextErrMessage.setText("身份核验失败");
            mTextErrTips.setText("");
        } else if (errCode == FaceException.ErrorCode.VERIFY_NO_PICTURE_50
                || errCode == FaceException.ErrorCode.VERIFY_NO_PICTURE_55
                || verifyStatus == 2) {
            // 公安网身份信息覆盖不全
            mTextErrMessage.setText("公安网不存在或质量低");
            mTextErrTips.setText("请到派出所更新身份证图片");
            mBtnReturnHome.setText("关闭");
            mBtnReCollect.setVisibility(View.GONE);
            // 活体检测未通过
        } else if (errCode == FaceException.ErrorCode.VERIFY_LIVENESS_FAILURE) {
            mTextErrMessage.setText("身份核验失败");
            mTextErrTips.setText("请确保是本人操作且正脸采集");
            // 姓名与身份证不匹配或分值低
        } else if (userInfoError(errCode, verifyStatus)) {
            mTextErrMessage.setText("身份核验失败");
            mTextErrTips.setText("请确保是本人操作且正脸采集");
            // 质量检测未通过
        } else if (errCode == FaceException.ErrorCode.VERITY_QUALITY_FAILURE) {
            mTextErrMessage.setText("身份核验失败");
            mTextErrTips.setText("请确保正脸采集且清晰完整");
        } else if (netWorkError(errCode)) {
            mTextErrMessage.setText("网络连接失败");
            mTextErrTips.setText("请检查网络");
            mBtnReturnHome.setText("重试");
            mBtnReCollect.setText("退出核验");
            mImageError.setImageResource(cn.yanhu.commonres.R.mipmap.icon_verify_network);
        } else {
            // 其他错误
            mTextErrMessage.setText("身份核验失败");
            mTextErrTips.setText("");
//            mTextErrMessage.setText("参数格式错误");
//            mTextErrTips.setText("请参考API说明文档，修改参数");
//            mBtnReturnHome.setText("关闭");
//            mBtnReCollect.setVisibility(View.GONE);
//            mImageError.setImageResource(R.mipmap.icon_verify_network);
        }
    }

    private boolean userInfoError(int errCode, int verifyStatus) {
        // 减少方法圈数 BugBye MethodComplexityCheck
        return errCode == FaceException.ErrorCode.VERITY_NO_MATCH_51
                || errCode == FaceException.ErrorCode.VERITY_NO_MATCH_54
                || errCode == FaceException.ErrorCode.VERITY_NO_MATCH_60
                || errCode == FaceException.ErrorCode.LOW_SCORE
                || verifyStatus == 1;
    }

    private boolean netWorkError(int errCode) {
        return  errCode == FaceException.ErrorCode.JSON_PARSE_ERROR
                || errCode == FaceException.ErrorCode.NETWORK_REQUEST_ERROR
                || errCode == FaceException.ErrorCode.VERIFY_NET_ERROR_222361
                || errCode == FaceException.ErrorCode.VERIFY_NET_ERROR_282105;
    }

    public void onCloseVerify(View v) {
        finish();
    }

    public void onRecollect(View v) {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
