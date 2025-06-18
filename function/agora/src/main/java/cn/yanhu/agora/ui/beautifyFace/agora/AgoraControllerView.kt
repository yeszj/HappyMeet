package cn.yanhu.agora.ui.beautifyFace.agora

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import androidx.core.view.isVisible
import cn.happy.beautyface.R
import cn.happy.beautyface.bean.BeautyConfigCache
import cn.happy.beautyface.ui.widget.BaseControllerView
import cn.yanhu.commonres.manager.AppCacheManager
import com.blankj.utilcode.util.GsonUtils
import kotlin.compareTo
import kotlin.text.toFloat
import kotlin.text.toInt

class AgoraControllerView : BaseControllerView {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun resetPageList() {
        pageList = onPageListCreate()
    }

    override fun onPageListCreate(): List<PageInfo> {

        if (!AgoraBeautySDK.isInitBeautyConfig()) {
            val beautyConfigCacheInfo = AppCacheManager.beautyConfigCache
            if (!TextUtils.isEmpty(beautyConfigCacheInfo)) {
                AgoraBeautySDK.beautyConfigCache = GsonUtils.fromJson<BeautyConfigCache>(
                    beautyConfigCacheInfo,
                    BeautyConfigCache::class.java
                )
            }
            AgoraBeautySDK.beautyConfig = AgoraBeautySDK.BeautyConfig()
        }

        val beautyConfig = AgoraBeautySDK.beautyConfig
        return listOf(
            PageInfo(
                R.string.show_beauty_group_beauty,
                listOf(
                    ItemInfo(
                        R.string.show_beauty_item_none,
                        R.mipmap.show_beauty_ic_none,
                        isSelected = !beautyConfig.basicBeauty,
                        onValueChanged = { _ ->
                            beautyConfig.basicBeauty = false
                        }
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_adjust_contrast,
                        R.mipmap.show_beauty_ic_adjust_contrast,
                        beautyConfig.adjustContrast,
                        valueRange = -1.0f..1.0f,
                        onValueChanged = { value ->
                            beautyConfig.adjustContrast = value
                        }
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_beauty_smooth,
                        R.mipmap.show_beauty_ic_face_mopi,
                        beautyConfig.smooth,
                        isSelected = beautyConfig.basicBeauty,
                        onValueChanged = { value ->
                            beautyConfig.smooth = value
                        }
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_beauty_whiten,
                        R.mipmap.show_beauty_ic_face_meibai,
                        beautyConfig.whiten,
                        onValueChanged = { value ->
                            beautyConfig.whiten = value
                        }
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_beauty_redden,
                        R.mipmap.show_beauty_ic_face_redden,
                        beautyConfig.redden,
                        onValueChanged = { value ->
                            beautyConfig.redden = value
                        }
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_beauty_remove_dark_circles,
                        R.mipmap.show_beauty_ic_face_remove_dark_circles,
                        beautyConfig.darkCircles,
                        onValueChanged = { value ->
                            beautyConfig.darkCircles = value
                        }
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_beauty_remove_nasolabial_folds,
                        R.mipmap.show_beauty_ic_face_remove_nasolabial_folds,
                        beautyConfig.nasolabialFolds,
                        onValueChanged = { value ->
                            beautyConfig.nasolabialFolds = value
                        }
                    ),
                )
            ),
            PageInfo(
                R.string.show_beauty_group_face_shape,
                listOf(
                    ItemInfo(
                        R.string.show_beauty_item_none,
                        R.mipmap.show_beauty_ic_none,
                        onValueChanged = { _ ->
                            beautyConfig.faceShape = false
                        },
                        isSelected = !AgoraBeautySDK.beautyConfig.basicBeauty
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_beauty_eye,
                        R.mipmap.show_beauty_ic_face_eye,
                        beautyConfig.enlargeEye.toFloat(),
                        onValueChanged = { value ->
                            beautyConfig.enlargeEye = value.toInt()
                        },
                        isSelected = AgoraBeautySDK.beautyConfig.basicBeauty,
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_beauty_chin,
                        R.mipmap.show_beauty_ic_face_xiaba,
                        beautyConfig.chinLength.toFloat(),
                        onValueChanged = { value ->
                            beautyConfig.chinLength = value.toInt()
                        },
                        valueRange = -100f..100f
                    ),

                    ItemInfo(
                        R.string.show_beauty_item_beauty_overall,
                        R.mipmap.show_beauty_ic_face_shoulian,
                        beautyConfig.thinFace.toFloat(),
                        onValueChanged = { value ->
                            beautyConfig.thinFace = value.toInt()
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_beauty_narrowFace,
                        R.drawable.svg_narrow_face,
                        beautyConfig.narrowFace.toFloat(),
                        onValueChanged = { value ->
                            beautyConfig.narrowFace = value.toInt()
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_beauty_vFace,
                        R.drawable.svg_narrow_face,
                        beautyConfig.mandible.toFloat(),
                        onValueChanged = { value ->
                            beautyConfig.mandible = value.toInt()
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_beauty_headsscale,
                        R.drawable.svg_small_head,
                        beautyConfig.headScale.toFloat(),
                        onValueChanged = { value ->
                            beautyConfig.headScale = value.toInt()
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_beauty_cheekbone,
                        R.mipmap.show_beauty_ic_face_shouquangu,
                        beautyConfig.shrinkCheekbone.toFloat(),
                        onValueChanged = { value ->
                            beautyConfig.shrinkCheekbone = value.toInt()
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_beauty_long_nose,
                        R.mipmap.show_beauty_ic_face_changbi,
                        beautyConfig.longNose.toFloat(),
                        onValueChanged = { value ->
                            beautyConfig.longNose = value.toInt()
                        },
                        valueRange = -100f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_beauty_nose,
                        R.mipmap.show_beauty_ic_face_shoubi,
                        beautyConfig.narrowNose.toFloat(),
                        onValueChanged = { value ->
                            beautyConfig.narrowNose = value.toInt()
                        },
                        valueRange = -100f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_beauty_mouth,
                        R.mipmap.show_beauty_ic_face_zuixing,
                        beautyConfig.mouthSize.toFloat(),
                        onValueChanged = { value ->
                            beautyConfig.mouthSize = value.toInt()
                        },
                        valueRange = -100f..100f
                    ),
//                    ItemInfo(
//                        R.string.show_beauty_item_beauty_xiahexian,
//                        R.mipmap.show_beauty_ic_face_xiahexian,
//                        beautyConfig.mouthSize.toFloat(),
//                        onValueChanged = { value ->
//                            // TODO:
//                        }
//                    ),
                    ItemInfo(
                        R.string.show_beauty_item_beauty_jawbone,
                        R.mipmap.show_beauty_ic_face_xiahegu,
                        beautyConfig.shrinkJawbone.toFloat(),
                        onValueChanged = { value ->
                            beautyConfig.shrinkJawbone = value.toInt()
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_beauty_fajixian,
                        R.mipmap.show_beauty_ic_face_etou,
                        beautyConfig.hairlineHeight.toFloat(),
                        onValueChanged = { value ->
                            beautyConfig.hairlineHeight = value.toInt()
                        },
                        valueRange = -100f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_gentleman_face,
                        R.mipmap.show_beauty_ic_face_gentleman,
                        beautyConfig.gentlemanFace.toFloat(),
                        onValueChanged = { value ->
                            beautyConfig.gentlemanFace = value.toInt()
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_lady_face,
                        R.mipmap.show_beauty_ic_face_lady,
                        beautyConfig.ladyFace.toFloat(),
                        onValueChanged = { value ->
                            beautyConfig.ladyFace = value.toInt()
                        },
                        valueRange = 0f..100f
                    ),
                )
            ),
            PageInfo(
                R.string.show_beauty_group_effect,
                listOf(
                    ItemInfo(
                        R.string.show_beauty_item_none,
                        R.mipmap.show_beauty_ic_none,
                        isSelected = beautyConfig.makeupType == 0,
                        onValueChanged = { _ ->
                            beautyConfig.makeupType = 0
                        }
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_effect_hunxue,
                        R.mipmap.show_beauty_ic_effect_oumei,
                        withPadding = false,
                        isSelected = beautyConfig.makeupType == 1,
                        value = beautyConfig.makeupStrength,
                        onValueChanged = { value ->
                            beautyConfig.makeupType = 1
                            beautyConfig.makeupStrength = value
                        }
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_effect_oumei,
                        R.mipmap.show_beauty_ic_effect_oumei,
                        withPadding = false,
                        isSelected = beautyConfig.makeupType == 2,
                        value = beautyConfig.makeupStrength,
                        onValueChanged = { value ->
                            beautyConfig.makeupType = 2
                            beautyConfig.makeupStrength = value
                        }
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_effect3,
                        R.mipmap.show_beauty_ic_effect_oumei,
                        withPadding = false,
                        isSelected = beautyConfig.makeupType == 3,
                        value = beautyConfig.makeupStrength,
                        onValueChanged = { value ->
                            beautyConfig.makeupType = 3
                            beautyConfig.makeupStrength = value
                        }
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_effect4,
                        R.mipmap.show_beauty_ic_effect_oumei,
                        withPadding = false,
                        isSelected = beautyConfig.makeupType == 4,
                        value = beautyConfig.makeupStrength,
                        onValueChanged = { value ->
                            beautyConfig.makeupType = 4
                            beautyConfig.makeupStrength = value
                        }
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_effect5,
                        R.mipmap.show_beauty_ic_effect_oumei,
                        withPadding = false,
                        isSelected = beautyConfig.makeupType == 5,
                        value = beautyConfig.makeupStrength,
                        onValueChanged = { value ->
                            beautyConfig.makeupType = 5
                            beautyConfig.makeupStrength = value
                        }
                    ),
                )
            ),
            PageInfo(
                R.string.show_beauty_group_filter,
                listOf(
                    ItemInfo(
                        R.string.show_beauty_item_none,
                        R.mipmap.show_beauty_ic_none,
                        isSelected = beautyConfig.filterType == AgoraBeautySDK.FilterStyle.None,
                        onValueChanged = { _ ->
                            beautyConfig.filterType = AgoraBeautySDK.FilterStyle.None
                        }
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_nuannan,
                        R.mipmap.show_beauty_ic_effect_baixi,
                        withPadding = false,
                        isSelected = beautyConfig.filterType == AgoraBeautySDK.FilterStyle.NUANNAN,
                        value = beautyConfig.filterStrength,
                        onValueChanged = { value ->
                            beautyConfig.filterType = AgoraBeautySDK.FilterStyle.NUANNAN
                            beautyConfig.filterStrength = value
                        }
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_qise,
                        R.mipmap.show_beauty_ic_effect_qise,
                        withPadding = false,
                        isSelected = beautyConfig.filterType == AgoraBeautySDK.FilterStyle.QISE,
                        value = beautyConfig.filterStrength,
                        onValueChanged = { value ->
                            beautyConfig.filterType = AgoraBeautySDK.FilterStyle.QISE
                            beautyConfig.filterStrength = value
                        }
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_qizhi,
                        R.mipmap.show_beauty_ic_effect_cwei,
                        withPadding = false,
                        isSelected = beautyConfig.filterType == AgoraBeautySDK.FilterStyle.QIZHI,
                        value = beautyConfig.filterStrength,
                        onValueChanged = { value ->
                            beautyConfig.filterType = AgoraBeautySDK.FilterStyle.QIZHI
                            beautyConfig.filterStrength = value
                        }
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_yuanqi,
                        R.mipmap.show_beauty_ic_effect_yuanqi,
                        withPadding = false,
                        isSelected = beautyConfig.filterType == AgoraBeautySDK.FilterStyle.YUANQI,
                        value = beautyConfig.filterStrength,
                        onValueChanged = { value ->
                            beautyConfig.filterType = AgoraBeautySDK.FilterStyle.YUANQI
                            beautyConfig.filterStrength = value
                        }
                    ),
                )
            ),
            PageInfo(
                R.string.show_beauty_group_adjust,
                listOf(
                    ItemInfo(
                        R.string.show_beauty_item_none,
                        R.mipmap.show_beauty_ic_none,
                        0.0f,
                        isSelected = beautyConfig.sharpen > 0,
                        onValueChanged = { _ ->
                            beautyConfig.sharpen = 0.0f
                        },
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_adjust_sharpen,
                        R.mipmap.show_beauty_ic_adjust_sharp,
                        beautyConfig.sharpen,
                        isSelected = beautyConfig.sharpen > 0,
                        onValueChanged = { value ->
                            beautyConfig.sharpen = value
                        }
                    ),
                )
            )
        )
    }

    override fun onSelectedChanged(pageIndex: Int, itemIndex: Int) {
        super.onSelectedChanged(pageIndex, itemIndex)
        val pageInfo = pageList[pageIndex]
        val itemInfo = pageInfo.itemList[itemIndex]
        if (itemInfo.name == R.string.show_beauty_item_none) {
            viewBinding.slider.visibility = INVISIBLE
            viewBinding.ivCompare.isVisible = false
        } else if (pageInfo.name == R.string.show_beauty_group_beauty
            || pageInfo.name == R.string.show_beauty_group_face_shape
            || pageInfo.name == R.string.show_beauty_group_effect
            || pageInfo.name == R.string.show_beauty_group_filter
            || pageInfo.name == R.string.show_beauty_group_adjust
        ) {
            viewBinding.slider.visibility = VISIBLE
            viewBinding.ivCompare.isVisible = true
        }
    }

}