package cn.yanhu.agora.manager;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.SPUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cn.yanhu.agora.R;
import cn.huanyuan.sweetlove.authpack;
import cn.yanhu.agora.bean.BeautyBean;
import io.agora.rtc2.RtcEngine;

public class BeautySetManager {

    private static final class InstanceHolder {
        static final BeautySetManager instance = new BeautySetManager();
    }

    public static BeautySetManager getInstance() {
        return InstanceHolder.instance;
    }

    private BeautySetManager() {
    }

    //获取美颜设置
    public List<BeautyBean> getBeautyList(){
        String beautySetList = SPUtils.getInstance().getString("beautySetList", "");
        if(!beautySetList.isEmpty()){
            return new Gson().fromJson(beautySetList,new TypeToken<List<BeautyBean>>(){}.getType());
        }else {
            List<BeautyBean> list = new ArrayList<>();
            //美肤
            list.add(new BeautyBean(R.mipmap.ic_beauty_set_skin,"磨皮","blur_level",90));
            list.add(new BeautyBean(R.mipmap.ic_beauty_set_whitening,"美白","color_level",70));
            list.add(new BeautyBean(R.mipmap.ic_beauty_set_ruddy,"红润","red_level",50));
            list.add(new BeautyBean(R.mipmap.ic_beauty_set_sharpen,"锐化","sharpen",0));
            list.add(new BeautyBean(R.mipmap.ic_beauty_set_face,"去法令纹","remove_nasolabial_folds_strength",30));

            //美型
            list.add(new BeautyBean(R.mipmap.ic_beauty_set_eye,"大眼","eye_enlarging",30));
            list.add(new BeautyBean(R.mipmap.ic_beauty_set_thin,"廋脸","cheek_thinning",50));
            list.add(new BeautyBean(R.mipmap.ic_beauty_set_v,"V脸","cheek_v",50));
            list.add(new BeautyBean(R.mipmap.ic_beauty_set_narrow,"窄脸","cheek_narrow",10));
            list.add(new BeautyBean(R.mipmap.ic_beauty_set_short,"短脸","intensity_chin",0));
            list.add(new BeautyBean(R.mipmap.ic_beauty_set_small,"小脸","cheek_small",10));
            return list;
        }
    }


    public void setBeautyProperty(String key, int value) {
        Double aDouble = toDouble(value, 100);

        if (key.equals("blur_level")) {//磨皮
            setParam(key, toDouble(value, 100) * 6);

        } else if (key.equals("color_level")) {//美白
            setParam(key, toDouble(value, 50));

        } else if (key.equals("red_level")) {//红润
            setParam(key, toDouble(value, 50));

        } else if (key.equals("sharpen")) {//锐化
            setParam(key, aDouble);

        } else if (key.equals("remove_nasolabial_folds_strength")) {//去除法令纹
            setParam(key, aDouble);

        } else if (key.equals("eye_enlarging")) {//大眼
            setParam(key, aDouble);

        } else if (key.equals("cheek_thinning")) {//瘦脸
            setParam(key, aDouble);

        } else if (key.equals("cheek_v")) {//V脸
            setParam(key, aDouble);

        } else if (key.equals("cheek_narrow")) {//窄脸
            setParam(key, aDouble);

        } else if (key.equals("intensity_chin")) {//下巴调整
            setParam(key, toDouble(value, 200));

        } else if (key.equals("cheek_small")) {//小脸
            setParam(key, aDouble);

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

    //修正参数值
    private Double toDouble(int numerator, int denominator) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(new Locale("zh", "rCN")));
        return Double.parseDouble(decimalFormat.format((Double.valueOf(numerator) / denominator)));
    }

    //设置属性
    private void setExtensionProperty(String key, String property) {
        if (mRtcEngine!=null){
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

    private File getExternalFilesDir(){
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
        for (BeautyBean beautyBean : getBeautyList()) {
            setBeautyProperty(beautyBean.getKey(),beautyBean.getValue());
        }
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
