package cn.yanhu.imchat.custom.message.chatRelatiionshipView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.GsonUtils;

import cn.yanhu.imchat.custom.message.BaseEaseChatRow;
import cn.yanhu.commonres.config.ChatConstant;
import cn.yanhu.imchat.R;
import cn.yanhu.imchat.bean.RelationshipBindImInfo;
import cn.yanhu.imchat.manager.RelationshipTypeManager;

@SuppressLint("ViewConstructor")
public class ChatRelationshipView extends BaseEaseChatRow {
    private TextView tvRelationShip;
    private ImageView ivRelationship;

    private TextView tvAlert;

    public ChatRelationshipView(Context context, boolean isSender) {
        super(context, isSender);
    }

    @Override
    protected void onInflateView() {
        inflater.inflate(R.layout.ease_bind_relationship_layout, this);
    }

    @Override
    protected void onFindViewById() {
        tvRelationShip = findViewById(R.id.tv_relationShip);
        ivRelationship = findViewById(R.id.iv_relationship);
        tvAlert = findViewById(R.id.tv_alert);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onSetUpView() {
        super.onSetUpView();
        try {
            String stringAttribute = message.getStringAttribute(ChatConstant.CUSTOM_MSG, "");
            RelationshipBindImInfo relationshipBindImInfo = GsonUtils.fromJson(stringAttribute, RelationshipBindImInfo.class);
            int relationshipType = relationshipBindImInfo.getRelationshipType();
            if (relationshipType == RelationshipTypeManager.TYPE_INTIMATE_FRIEND) {
                tvRelationShip.setText("密友");
                tvRelationShip.setTextColor(Color.parseColor("#6D91FF"));
                ivRelationship.setImageResource(R.drawable.icon_relationship_select1);
            } else if (relationshipType == RelationshipTypeManager.TYPE_GOOD_FRIEND) {
                tvRelationShip.setText("好友");
                tvRelationShip.setTextColor(Color.parseColor("#FF8C00"));
                ivRelationship.setImageResource(R.drawable.icon_relationship_select2);
            }else if (relationshipType == RelationshipTypeManager.TYPE_DARLING) {
                tvRelationShip.setText("心肝");
                tvRelationShip.setTextColor(Color.parseColor("#FF8C00"));
                ivRelationship.setImageResource(R.drawable.icon_relationship_select2);
            } else if (relationshipType == RelationshipTypeManager.TYPE_LOVER) {
                tvRelationShip.setText("恋人");
                tvRelationShip.setTextColor(Color.parseColor("#FF2A68"));
                ivRelationship.setImageResource(R.drawable.icon_relationship_select3);
            }
            tvAlert.setText("恭喜，您与" + relationshipBindImInfo.getNickName() + "已成为");
        } catch (Exception e) {
        }
    }
}
