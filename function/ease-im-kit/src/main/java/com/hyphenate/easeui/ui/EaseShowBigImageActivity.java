/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hyphenate.easeui.ui;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMError;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMImageMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.EaseIM;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.constants.EaseConstant;
import com.hyphenate.easeui.domain.EaseEmojicon;
import com.hyphenate.easeui.ui.base.EaseBaseActivity;
import com.hyphenate.easeui.utils.EaseFileUtils;
import com.hyphenate.easeui.widget.photoview.EasePhotoView;
import com.hyphenate.util.EMLog;

/**
 * download and show original image
 * 
 */
public class EaseShowBigImageActivity extends EaseBaseActivity {
	private static final String TAG = "ShowBigImage"; 
	private ProgressDialog pd;
	private EasePhotoView image;
	private int default_res = R.drawable.ease_default_image;
	private String filename;
	private Bitmap bitmap;
	private String emojiIconId = "";
	private boolean isDownloaded;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.ease_activity_show_big_image);
		super.onCreate(savedInstanceState);
		setFitSystemForTheme(true, R.color.black, false);
		image = (EasePhotoView) findViewById(R.id.image);
		ProgressBar loadLocalPb = (ProgressBar) findViewById(R.id.pb_load_local);
		default_res = getIntent().getIntExtra("default_image", R.drawable.ease_default_avatar);
		Uri uri = getIntent().getParcelableExtra("uri");
		filename = getIntent().getExtras().getString("filename");
		String msgId = getIntent().getExtras().getString("messageId");
		if (getIntent().hasExtra(EaseConstant.MESSAGE_ATTR_EXPRESSION_ID)){
			emojiIconId = getIntent().getExtras().getString(EaseConstant.MESSAGE_ATTR_EXPRESSION_ID);
		}
		EMLog.d(TAG, "show big msgId:" + msgId );

		//show the image if it exist in local path
		if (EaseFileUtils.isFileExistByUri(this, uri)) {
            Glide.with(this).load(uri).into(image);
		} else if (!TextUtils.isEmpty(emojiIconId)){
			showBigExpression(emojiIconId);
		} else if(msgId != null) {
			EMMessage msg = EMClient.getInstance().chatManager().getMessage(msgId);
			if(msg == null) {
				msg = getIntent().getParcelableExtra("msg");
				if(msg == null) {
					EMLog.e(TAG, "message is null, messageId: " + msgId);
					finish();
					return;
				}
			}
			EMImageMessageBody body = (EMImageMessageBody) msg.getBody();
			if(EaseFileUtils.isFileExistByUri(this, body.getLocalUri())) {
				Glide.with(this).load(body.getLocalUri()).into(image);
			}else {
				downloadImage(msg);
			}
		}else {
			image.setImageResource(default_res);
		}
	}

	/**
	 * 展示自定义表情图片
	 * @param emojiIconId 表情对应id
	 */
	private void showBigExpression(String emojiIconId){
		EaseEmojicon emojiIcon = null;
		if(EaseIM.getInstance().getEmojiconInfoProvider() != null){
			emojiIcon =  EaseIM.getInstance().getEmojiconInfoProvider().getEmojiconInfo(emojiIconId);
			if(emojiIcon != null){
				if(emojiIcon.getBigIcon() != 0){

					Glide.with(this).load(emojiIcon.getBigIcon())
							.apply(RequestOptions.placeholderOf(R.drawable.ease_default_expression))
							.into(image);
				}else if(emojiIcon.getBigIconPath() != null){
					Glide.with(this).load(emojiIcon.getBigIconPath())
							.apply(RequestOptions.placeholderOf(R.drawable.ease_default_expression))
							.into(image);
				}else{
					image.setImageResource(R.drawable.ease_default_expression);
				}
			}
		}
	}

	/**
	 * download image
	 * 
	 * @param msg
	 */
	@SuppressLint("NewApi")
	private void downloadImage(final EMMessage msg) {
        EMLog.e(TAG, "download with messageId: " + msg.getMsgId());
		String str1 = getResources().getString(R.string.Download_the_pictures);
		pd = new ProgressDialog(this);
		pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pd.setCanceledOnTouchOutside(false);
		pd.setMessage(str1);
		pd.show();
        final EMCallBack callback = new EMCallBack() {
			public void onSuccess() {
			    EMLog.e(TAG, "onSuccess" );
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (!isFinishing() && !isDestroyed()) {
							if (pd != null) {
								pd.dismiss();
							}
							isDownloaded = true;
							Uri localUrlUri = ((EMImageMessageBody) msg.getBody()).getLocalUri();
							Glide.with(EaseShowBigImageActivity.this)
									.load(localUrlUri)
									.apply(new RequestOptions().error(default_res))
									.into(image);
						}
					}
				});
			}

			public void onError(final int error, String message) {
				EMLog.e(TAG, "offline file transfer error:" + message);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (EaseShowBigImageActivity.this.isFinishing() || EaseShowBigImageActivity.this.isDestroyed()) {
						    return;
						}
                        image.setImageResource(default_res);
                        pd.dismiss();
                        if (error == EMError.FILE_NOT_FOUND) {
							Toast.makeText(getApplicationContext(), R.string.Image_expired, Toast.LENGTH_SHORT).show();
						}
					}
				});
			}

			public void onProgress(final int progress, String status) {
				EMLog.d(TAG, "Progress: " + progress);
				final String str2 = getResources().getString(R.string.Download_the_pictures_new);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
                        if (EaseShowBigImageActivity.this.isFinishing() || EaseShowBigImageActivity.this.isDestroyed()) {
                            return;
                        }
						pd.setMessage(str2 + progress + "%");
					}
				});
			}
		};
		

		msg.setMessageStatusCallback(callback);

		EMClient.getInstance().chatManager().downloadAttachment(msg);
	}

	@Override
	public void onBackPressed() {
		if (isDownloaded)
			setResult(RESULT_OK);
		finish();
	}
}
