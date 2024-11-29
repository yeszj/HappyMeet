package cn.yanhu.agora.manager;

import android.text.TextUtils;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

import cn.huanyuan.sweetlove.authpack;
import cn.yanhu.agora.bean.BeautyBean;
import cn.yanhu.agora.manager.dbCache.BeautyFaceParamCacheManager;
import cn.yanhu.commonres.manager.AppCacheManager;
import io.agora.rtc2.RtcEngine;

public class BeautySetManager {
    //美肤参数
    String skinJson = "[{\"type\":1,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_box_buffing_close_normal.webp\",\"checkIcon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_box_buffing_close_checked.webp\",\"name\":\"精细磨皮\",\"key\":\"blur_level\",\"value\":100},{\"type\":1,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_box_color_close_normal.webp\",\"checkIcon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_box_color_close_checked.webp\",\"name\":\"美白\",\"key\":\"color_level\",\"value\":100},{\"type\":1,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_box_red_close_normal.webp\",\"checkIcon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_box_red_close_checked.webp\",\"name\":\"红润\",\"key\":\"red_level\",\"value\":20},{\"type\":1,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_box_eyes_bright_close_normal.webp\",\"checkIcon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_box_eyes_bright_close_checked.webp\",\"name\":\"亮眼\",\"key\":\"eye_bright\",\"value\":20},{\"type\":1,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_box_teeth_close_normal.webp\",\"checkIcon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_box_teeth_close_checked.webp\",\"name\":\"美牙\",\"key\":\"tooth_whiten\",\"value\":40},{\"type\":1,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_box_dark_circles_close_normal.webp\",\"checkIcon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_box_dark_circles_close_checked.webp\",\"name\":\"去黑眼圈\",\"key\":\"remove_pouch_strength\",\"value\":10},{\"type\":1,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_box_wrinkle_close_normal.webp\",\"checkIcon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_box_wrinkle_close_checked.webp\",\"name\":\"去法令纹\",\"key\":\"remove_nasolabial_folds_strength\",\"value\":10}]";
    //美型参数
    String skinTypeJson = "[{\"type\":2,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_box_face_cheekthin_level_close_normal.webp\",\"checkIcon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_box_face_cheekthin_level_close_checked.webp\",\"name\":\"廋脸\",\"key\":\"cheek_thinning\",\"value\":50},{\"type\":2,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_box_face_v_close_normal.webp\",\"checkIcon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_box_face_v_close_checked.webp\",\"name\":\"V脸\",\"key\":\"cheek_v\",\"value\":10},{\"type\":2,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_box_face_narrow_close_normal.webp\",\"checkIcon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_box_face_narrow_close_checked.webp\",\"name\":\"窄脸\",\"key\":\"cheek_narrow\",\"value\":0},{\"type\":2,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_box_face_short_close_normal.webp\",\"checkIcon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_box_face_short_close_checked.webp\",\"name\":\"小脸\",\"key\":\"cheek_small\",\"value\":0},{\"type\":2,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_box_enlarge_eye_level_close_normal.webp\",\"checkIcon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_box_enlarge_eye_level_close_checked.webp\",\"name\":\"大眼\",\"key\":\"eye_enlarging\",\"value\":30},{\"type\":2,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_box_chin_close_normal.webp\",\"checkIcon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_box_chin_close_checked.webp\",\"name\":\"下巴\",\"key\":\"intensity_chin\",\"value\":20},{\"type\":2,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_box_forehead_close_normal.webp\",\"checkIcon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_box_forehead_close_checked.webp\",\"name\":\"额头\",\"key\":\"intensity_forehead\",\"value\":20},{\"type\":2,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_box_thin_nose_close_normal.webp\",\"checkIcon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_box_thin_nose_close_checked.webp\",\"name\":\"瘦鼻\",\"key\":\"intensity_nose\",\"value\":50},{\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_box_mouth_bones_close_normal.webp\",\"checkIcon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_box_mouth_close_checked.webp\",\"name\":\"嘴型\",\"key\":\"intensity_mouth\",\"value\":10},{\"type\":2,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_box_eyes_bright_close_normal.webp\",\"checkIcon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_box_eyes_bright_close_checked.webp\",\"name\":\"开眼角\",\"key\":\"intensity_canthus\",\"value\":0}]";
    //滤镜配置
    String skinFilterJson = "[{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_cancel.webp\",\"checkIcon\":\"\",\"name\":\"原图\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"origin\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_natural_1.webp\",\"checkIcon\":\"\",\"name\":\"自然1\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"ziran1\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_natural_2.webp\",\"checkIcon\":\"\",\"name\":\"自然2\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"ziran2\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_natural_3.webp\",\"checkIcon\":\"\",\"name\":\"自然3\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"ziran3\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_natural_4.webp\",\"checkIcon\":\"\",\"name\":\"自然4\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"ziran4\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_natural_5.webp\",\"checkIcon\":\"\",\"name\":\"自然5\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"ziran5\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_natural_6.webp\",\"checkIcon\":\"\",\"name\":\"自然6\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"ziran6\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_natural_7.webp\",\"checkIcon\":\"\",\"name\":\"自然7\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"ziran7\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_natural_8.webp\",\"checkIcon\":\"\",\"name\":\"自然8\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"ziran8\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_texture_gray_1.webp\",\"checkIcon\":\"\",\"name\":\"质感灰1\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"zhiganhui1\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_texture_gray_2.webp\",\"checkIcon\":\"\",\"name\":\"质感灰2\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"zhiganhui2\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_texture_gray_3.webp\",\"checkIcon\":\"\",\"name\":\"质感灰3\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"zhiganhui3\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_texture_gray_4.webp\",\"checkIcon\":\"\",\"name\":\"质感灰4\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"zhiganhui4\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_texture_gray_5.webp\",\"checkIcon\":\"\",\"name\":\"质感灰5\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"zhiganhui5\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_texture_gray_6.webp\",\"checkIcon\":\"\",\"name\":\"质感灰6\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"zhiganhui6\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_texture_gray_7.webp\",\"checkIcon\":\"\",\"name\":\"质感灰7\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"zhiganhui7\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_texture_gray_8.webp\",\"checkIcon\":\"\",\"name\":\"质感灰8\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"zhiganhui1\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_bailiang_1.webp\",\"checkIcon\":\"\",\"name\":\"白亮1\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"bailiang1\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_bailiang_2.webp\",\"checkIcon\":\"\",\"name\":\"白亮2\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"bailiang2\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_bailiang_3.webp\",\"checkIcon\":\"\",\"name\":\"白亮3\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"bailiang3\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_bailiang_4.webp\",\"checkIcon\":\"\",\"name\":\"白亮4\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"bailiang4\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_bailiang_5.webp\",\"checkIcon\":\"\",\"name\":\"白亮5\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"bailiang5\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_bailiang_6.webp\",\"checkIcon\":\"\",\"name\":\"白亮6\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"bailiang6\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_bailiang_7.webp\",\"checkIcon\":\"\",\"name\":\"白亮7\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"bailiang7\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_fennen_1.webp\",\"checkIcon\":\"\",\"name\":\"粉嫩1\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"fennen1\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_fennen_2.webp\",\"checkIcon\":\"\",\"name\":\"粉嫩2\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"fennen2\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_fennen_3.webp\",\"checkIcon\":\"\",\"name\":\"粉嫩3\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"fennen3\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_fennen_5.webp\",\"checkIcon\":\"\",\"name\":\"粉嫩5\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"fennen5\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_fennen_6.webp\",\"checkIcon\":\"\",\"name\":\"粉嫩6\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"fennen6\"},{\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_fennen_7.webp\",\"checkIcon\":\"\",\"name\":\"粉嫩7\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"fennen7\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_fennen_8.webp\",\"checkIcon\":\"\",\"name\":\"粉嫩8\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"fennen8\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_lengsediao_1.webp\",\"checkIcon\":\"\",\"name\":\"冷色调1\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"lengsediao1\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_lengsediao_2.webp\",\"checkIcon\":\"\",\"name\":\"冷色调2\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"lengsediao2\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_lengsediao_3.webp\",\"checkIcon\":\"\",\"name\":\"冷色调3\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"lengsediao3\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_lengsediao_7.webp\",\"checkIcon\":\"\",\"name\":\"冷色调4\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"lengsediao7\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_lengsediao_8.webp\",\"checkIcon\":\"\",\"name\":\"冷色调5\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"lengsediao8\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_lengsediao_11.webp\",\"checkIcon\":\"\",\"name\":\"冷色调6\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"lengsediao11\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_nuansediao_1.webp\",\"checkIcon\":\"\",\"name\":\"暖色调1\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"nuansediao1\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_nuansediao_2.webp\",\"checkIcon\":\"\",\"name\":\"暖色调2\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"nuansediao2\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_gexing_1.webp\",\"checkIcon\":\"\",\"name\":\"个性1\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"gexing1\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_gexing_2.webp\",\"checkIcon\":\"\",\"name\":\"个性2\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"gexing2\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_gexing_3.webp\",\"checkIcon\":\"\",\"name\":\"个性3\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"gexing3\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_gexing_4.webp\",\"checkIcon\":\"\",\"name\":\"个性4\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"gexing4\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_gexing_5.webp\",\"checkIcon\":\"\",\"name\":\"个性5\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"gexing1\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_gexing_7.webp\",\"checkIcon\":\"\",\"name\":\"个性6\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"gexing7\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_gexing_10.webp\",\"checkIcon\":\"\",\"name\":\"个性7\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"gexing10\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_gexing_11.webp\",\"checkIcon\":\"\",\"name\":\"个性8\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"gexing11\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_xiaoqingxin_1.webp\",\"checkIcon\":\"\",\"name\":\"小清新1\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"xiaoqingxin1\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_xiaoqingxin_3.webp\",\"checkIcon\":\"\",\"name\":\"小清新2\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"xiaoqingxin3\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_xiaoqingxin_4.webp\",\"checkIcon\":\"\",\"name\":\"小清新3\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"xiaoqingxin4\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_xiaoqingxin_6.webp\",\"checkIcon\":\"\",\"name\":\"小清新4\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"xiaoqingxin6\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_heibai_1.webp\",\"checkIcon\":\"\",\"name\":\"黑白1\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"heibai1\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_heibai_2.webp\",\"checkIcon\":\"\",\"name\":\"黑白2\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"heibai2\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_heibai_3.webp\",\"checkIcon\":\"\",\"name\":\"黑白3\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"heibai3\"},{\"type\":3,\"icon\":\"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/beautifaceIcon/icon_beauty_filter_heibai_4.webp\",\"checkIcon\":\"\",\"name\":\"黑白4\",\"key\":\"filter_level\",\"value\":40,\"filterName\":\"heibai4\"}]";

    private static final class InstanceHolder {
        static final BeautySetManager instance = new BeautySetManager();
    }

    public static BeautySetManager getInstance() {
        return InstanceHolder.instance;
    }

    private BeautySetManager() {
    }

    public List<BeautyBean> getOriginSkinCareList() {
        return new Gson().fromJson(skinJson, new TypeToken<List<BeautyBean>>() {
        }.getType());
    }

    //获取美颜设置
    public List<BeautyBean> getSkinCareList() {
        List<BeautyBean> beautySetList = BeautyFaceParamCacheManager.INSTANCE.getBeautyParamsByType(1);
        if (beautySetList != null && beautySetList.size() > 0) {
            return beautySetList;
        } else {
            return getOriginSkinCareList();
        }
    }

    public List<BeautyBean> getOriginSkinTypList() {
        return new Gson().fromJson(skinTypeJson, new TypeToken<List<BeautyBean>>() {
        }.getType());
    }

    public List<BeautyBean> getSkinTypeList() {
        List<BeautyBean> beautySetList = BeautyFaceParamCacheManager.INSTANCE.getBeautyParamsByType(2);
        if (beautySetList != null && beautySetList.size() > 0) {
            return beautySetList;
        } else {
            return getOriginSkinTypList();
        }
    }

    public List<BeautyBean> getOriginSkinFilterList() {
        return new Gson().fromJson(skinFilterJson, new TypeToken<List<BeautyBean>>() {
        }.getType());
    }


    /**
     * 设置美颜滤镜参数
     * @param value 进度值
     * @param filterName 滤镜名称
     */
    public void setBeautyFilter(int value, String filterName) {
        setParam("filter_level", toDouble(value, 100));
        setParam("filter_name", filterName);
    }

    public void setBeautyProperty(String key, int value) {
        Double aDouble = toDouble(value, 100);
        switch (key) {
            case "blur_level": //磨皮
                setParam(key, toDouble(value, 100) * 6);
                break;
            case "color_level": //美白
            case "red_level": //红润
                setParam(key, toDouble(value, 50));
                break;
            case "intensity_chin": //下巴调整
                setParam(key, toDouble(value, 200));
                break;
            default:
                setParam(key, aDouble);
                break;
        }
    }

    /**
     * 设置美颜参数
     */
    public void setParam(String key, Double value) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("obj_handle", getBeautyDir());
            jsonObject.put("name", key);
            jsonObject.put("value", value);
            setExtensionProperty("fuItemSetParam", jsonObject.toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void setParam(String key, String value) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("obj_handle", getBeautyDir());
            jsonObject.put("name", key);
            jsonObject.put("value", value);
            setExtensionProperty("fuItemSetParam", jsonObject.toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    //修正参数值
    private Double toDouble(int numerator, int denominator) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(new Locale("zh", "rCN")));
        return Double.parseDouble(decimalFormat.format((Double.valueOf(numerator) / denominator)));
    }

    //设置属性
    private void setExtensionProperty(String key, String property) {
        if (mRtcEngine != null) {
            mRtcEngine.setExtensionProperty("FaceUnity", "Effect", key, property);
        }
    }

    //美颜道具包路径
    private String getBeautyDir() {
        File modelDir = new File(getExternalFilesDir(), "Resource/graphics/face_beautification.bundle");
        return modelDir.getAbsolutePath();
    }

    private RtcEngine mRtcEngine;

    //初始化美颜插件
    public void initExtension(RtcEngine mRtcEngine) {
        this.mRtcEngine = mRtcEngine;
        // Setup
        try {
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            for (byte it : authpack.A()) {
                jsonArray.put(it);
            }
            jsonObject.put("authdata", jsonArray);
            setExtensionProperty("fuSetup", jsonObject.toString());
            //加载道具包
            loadAIModels();
        } catch (JSONException e) {
            // lo(e.getMessage());

        }
    }

    private File getExternalFilesDir() {
        return ActivityUtils.getTopActivity().getExternalFilesDir("assets");
    }

    //加载道具包
    private void loadAIModels() {

        // 加载 AI 模型
        try {
            File modelDir = new File(getExternalFilesDir(),
                    "Resource/model/ai_face_processor.bundle");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("data", modelDir.getAbsolutePath());
            // 通过 type 参数设置 AI 能力类型为 FUAITYPE_FACEPROCESSOR，对应取值为 1 << 8
            jsonObject.put("type", 1 << 8);
            setExtensionProperty("fuLoadAIModelFromPackage", jsonObject.toString());
        } catch (JSONException e) {
            //KLog.e(e.getMessage());

        }

        try {
            // Load AI model
            File modelDir = new File(getExternalFilesDir(), "Resource/model/ai_hand_processor.bundle");

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("data", modelDir.getAbsolutePath());
            jsonObject.put("type", 1 << 3);
            setExtensionProperty("fuLoadAIModelFromPackage", jsonObject.toString());
        } catch (JSONException e) {
            // KLog.e(e.getMessage());

        }

        try {
            // Load AI model
            File modelDir = new File(getExternalFilesDir(), "Resource/model/ai_human_processor_gpu.bundle");

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("data", modelDir.getAbsolutePath());
            jsonObject.put("type", 1 << 9);
            setExtensionProperty("fuLoadAIModelFromPackage", jsonObject.toString());
        } catch (JSONException e) {
            // KLog.e(e.getMessage());

        }

        try {
            File modelDir = new File(getExternalFilesDir(), "Resource/graphics/aitype.bundle");

            {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("data", modelDir.getAbsolutePath());
                setExtensionProperty("fuCreateItemFromPackage", jsonObject.toString());
            }

            {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("obj_handle", modelDir);
                jsonObject.put("name", "aitype");
                jsonObject.put("value", 1 << 8 | 1 << 30 | 1 << 3);
                setExtensionProperty("fuItemSetParam", jsonObject.toString());
            }
        } catch (JSONException e) {
            //KLog.e(e.getMessage());

        }
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("data", getBeautyDir());
            setExtensionProperty("fuCreateItemFromPackage", jsonObject.toString());
        } catch (JSONException e) {
            // KLog.e(e.getMessage());

        }
        setExtensionProperty("fuSetLogLevel", "FU_LOG_LEVEL_DEBUG");

    }

    /**
     * 开启美颜
     *
     * @param isEnable
     */
    public void enableBeauty(Boolean isEnable) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("obj_handle", getBeautyDir());
            jsonObject.put("name", "is_beauty_on");
            jsonObject.put("value", isEnable ? 1 : 0);
            setExtensionProperty("fuItemSetParam", jsonObject.toString());
            enableFaceShape();
        } catch (JSONException e) {
            //KLog.e(e.getMessage());

        }

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("obj_handle", getBeautyDir());
            jsonObject.put("name", "blur_type");
            jsonObject.put("value", 2);
            setExtensionProperty("fuItemSetParam", jsonObject.toString());
        } catch (JSONException e) {
            // KLog.e(e.getMessage());

        }

        //是否设置过美颜
//        if(SPUtils.getBoolean("isSetBeauty",false)){
//
//        }
        for (BeautyBean beautyBean : getSkinCareList()) {
            setBeautyProperty(beautyBean.getKey(), beautyBean.getValue());
        }
        for (BeautyBean beautyBean : getSkinTypeList()) {
            setBeautyProperty(beautyBean.getKey(), beautyBean.getValue());
        }
        String selectBeautyFilter = AppCacheManager.INSTANCE.getSelectBeautyFilter();

        if (TextUtils.isEmpty(selectBeautyFilter)) {
            List<BeautyBean> originSkinFilterList = getOriginSkinFilterList();
            BeautyBean beautyBean = originSkinFilterList.get(2);
            AppCacheManager.INSTANCE.setSelectBeautyFilter(GsonUtils.toJson(beautyBean));
            setBeautyFilter(beautyBean.getValue(), beautyBean.getFilterName());
        } else {
            BeautyBean beautyBean = GsonUtils.fromJson(selectBeautyFilter, BeautyBean.class);
            setBeautyFilter(beautyBean.getValue(), beautyBean.getFilterName());
        }
    }

    public void setSetDefaultFilter() {
        BeautySetManager.getInstance().setParam("filter_name", "origin");
    }

    /**
     * 是否开启美型
     */
    private void enableFaceShape() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("obj_handle", getBeautyDir());
            jsonObject.put("name", "face_shape");
            jsonObject.put("value", 4);
            setExtensionProperty("fuItemSetParam", jsonObject.toString());
        } catch (JSONException e) {
            // KLog.e(e.getMessage());
        }
    }

}
