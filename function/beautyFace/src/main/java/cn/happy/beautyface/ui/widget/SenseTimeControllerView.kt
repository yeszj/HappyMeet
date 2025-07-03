package cn.happy.beautyface.ui.widget

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import cn.happy.beautyface.ui.utils.SenseTimeBeautySDK
import cn.happy.beautyface.R
import cn.happy.beautyface.ui.utils.FileUtils

class SenseTimeControllerView : BaseControllerView {


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
        if (!SenseTimeBeautySDK.isInitBeautyConfig()) {
            SenseTimeBeautySDK.initDefaultBeautyConfig()
        }
        val beautyConfig = SenseTimeBeautySDK.beautyConfig
        return listOf(
            PageInfo(
                R.string.show_beauty_group_beauty,
                getBeautyList()
            ),
            PageInfo(
                R.string.show_beauty_group_face_shape,
                getFaceShapeList(beautyConfig)
            ),
            PageInfo(
                R.string.show_beauty_group_face_little_shape,
                getLittleFaceShapeList(beautyConfig)
            ),
            PageInfo(
                R.string.show_beauty_group_filter,
                getFilterList(context)
            ),

//            PageInfo(
//                R.string.show_beauty_group_adjust,
//                getGroupAdjustList()
//            ),
        )
    }

    override fun onSelectedChanged(pageIndex: Int, itemIndex: Int) {
        super.onSelectedChanged(pageIndex, itemIndex)
        val pageInfo = pageList[pageIndex]
        val itemInfo = pageInfo.itemList[itemIndex]
        if (itemInfo.nameId == R.string.show_beauty_item_none) {
            viewBinding.slider.visibility = INVISIBLE
          //  viewBinding.ivCompare.isVisible = false
        } else if (pageInfo.name == R.string.show_beauty_group_beauty
            || pageInfo.name == R.string.show_beauty_group_face_shape
            || pageInfo.name == R.string.show_beauty_group_effect
            || pageInfo.name == R.string.show_beauty_group_filter
            || pageInfo.name == R.string.show_beauty_group_face_little_shape
            || pageInfo.name == R.string.show_beauty_group_adjust
        ) {
            viewBinding.slider.visibility = VISIBLE
           // viewBinding.ivCompare.isVisible = true
        }
    }

}

private fun getFilterList(context: Context): List<BaseControllerView.ItemInfo> {
    // 人物
    val root =
        SenseTimeBeautySDK.getAssetsPath(context)
    val textureList = FileUtils.getFileItems("$root/filter_texture")

    val filterList = mutableListOf<BaseControllerView.ItemInfo>()
    filterList.add(
        BaseControllerView.ItemInfo(
        R.string.show_beauty_item_none,
        R.mipmap.show_beauty_ic_none,
        isSelected = TextUtils.isEmpty(SenseTimeBeautySDK.beautyConfig.filterMode),
        onValueChanged = { _ ->
            SenseTimeBeautySDK.beautyConfig.filterMode = null
        }
    ))
    textureList.forEach {
        filterList.add(
            BaseControllerView.ItemInfo(-1, -1, value = SenseTimeBeautySDK.beautyConfig.filterLength, isSelected = SenseTimeBeautySDK.beautyConfig.filterMode == it.model , withPadding = false,onValueChanged = { value ->
                if (SenseTimeBeautySDK.beautyConfig.filterMode !=it.model){
                    SenseTimeBeautySDK.beautyConfig.filterMode = it.model
                }
                SenseTimeBeautySDK.beautyConfig.filterLength = value
            }, iconPath = it.iconPath, name =it.name, filterModel =it.model)
        )
    }
    return filterList
}


private fun getBeautyList(): List<BaseControllerView.ItemInfo> = listOf(
    BaseControllerView.ItemInfo(
        R.string.show_beauty_item_none,
        R.mipmap.show_beauty_ic_none,
        isSelected = !SenseTimeBeautySDK.beautyConfigCache.beautyEnable,
        onValueChanged = { _ ->
            SenseTimeBeautySDK.clearBeauty()
        },
    ),
    BaseControllerView.ItemInfo(
        R.string.show_beauty_item_beauty_smooth,
        R.mipmap.show_beauty_ic_face_mopi,
        SenseTimeBeautySDK.beautyConfig.smooth,
        isSelected = SenseTimeBeautySDK.beautyConfigCache.beautyEnable,
        onValueChanged = { value ->
            SenseTimeBeautySDK.beautyConfig.smooth = value
        }
    ),

    BaseControllerView.ItemInfo(
        R.string.show_beauty_item_beauty_redden,
        R.mipmap.show_beauty_ic_face_redden,
        SenseTimeBeautySDK.beautyConfig.redden,
        onValueChanged = { value ->
            SenseTimeBeautySDK.beautyConfig.redden = value
        }
    ),
    BaseControllerView.ItemInfo(
        R.string.show_beauty_item_beauty_whiten,
        R.mipmap.show_beauty_ic_face_meibai,
        SenseTimeBeautySDK.beautyConfig.whiten,
        onValueChanged = { value ->
            SenseTimeBeautySDK.beautyConfig.whiten = value
        }
    ),


    BaseControllerView.ItemInfo(
        R.string.show_beauty_item_beauty_skin_smooth,
        R.mipmap.show_beauty_ic_face_mopi,
        SenseTimeBeautySDK.beautyConfig.skinSmooth,
        onValueChanged = { value ->
            SenseTimeBeautySDK.beautyConfig.skinSmooth = value
        }
    )
)

private fun getFaceShapeList(beautyConfig: SenseTimeBeautySDK.BeautyConfig): List<BaseControllerView.ItemInfo> =
    listOf(
        BaseControllerView.ItemInfo(
            R.string.show_beauty_item_none,
            R.mipmap.show_beauty_ic_none,
            isSelected = !SenseTimeBeautySDK.beautyConfigCache.faceShapeEnable,
            onValueChanged = { _ ->
                SenseTimeBeautySDK.clearShape()
            }
        ),
        BaseControllerView.ItemInfo(
            R.string.show_beauty_item_beauty_overall,
            R.mipmap.show_beauty_ic_face_shoulian,
            SenseTimeBeautySDK.beautyConfig.shrinkFace,
            isSelected = SenseTimeBeautySDK.beautyConfigCache.faceShapeEnable,
            onValueChanged = { value ->
                SenseTimeBeautySDK.beautyConfig.shrinkFace = value
            }
        ),
        BaseControllerView.ItemInfo(
            R.string.show_beauty_item_beauty_eye,
            R.mipmap.show_beauty_ic_face_eye,
            SenseTimeBeautySDK.beautyConfig.enlargeEye,
            onValueChanged = { value ->
                SenseTimeBeautySDK.beautyConfig.enlargeEye = value
            }
        ),
        BaseControllerView.ItemInfo(
            R.string.show_beauty_item_beauty_small_face,
            R.mipmap.show_beauty_ic_face_shoulian,
            SenseTimeBeautySDK.beautyConfig.smallFace,
            onValueChanged = { value ->
                SenseTimeBeautySDK.beautyConfig.smallFace = value
            }
        ),
        BaseControllerView.ItemInfo(
            R.string.show_beauty_item_beauty_narrowFace,
            R.drawable.svg_narrow_face,
            beautyConfig.narrowFace,
            onValueChanged = { value ->
                beautyConfig.narrowFace = value
            }
        ),
        BaseControllerView.ItemInfo(
            R.string.show_beauty_item_beauty_circle_eye,
            R.mipmap.show_beauty_ic_face_eye,
            beautyConfig.circleEye,
            onValueChanged = { value ->
                beautyConfig.circleEye = value
            }
        ),

        BaseControllerView.ItemInfo(
            R.string.show_beauty_item_beauty_round_face,
            R.mipmap.show_beauty_ic_face_liti,
            beautyConfig.roundThinFace,
            onValueChanged = { value ->
                beautyConfig.roundThinFace = value
            }
        ),
        BaseControllerView.ItemInfo(
            R.string.show_beauty_item_beauty_long_face,
            R.mipmap.show_beauty_ic_face_shoulian,
            beautyConfig.longThinFace,
            onValueChanged = { value ->
                beautyConfig.longThinFace = value
            }
        ),
        BaseControllerView.ItemInfo(
            R.string.show_beauty_item_beauty_lady_face,
            R.mipmap.show_beauty_ic_face_lady,
            beautyConfig.ladyThinFace,
            onValueChanged = { value ->
                beautyConfig.ladyThinFace = value
            }
        ),
        BaseControllerView.ItemInfo(
            R.string.show_beauty_item_beauty_natural_face,
            R.mipmap.show_beauty_ic_face_shoulian,
            beautyConfig.naturalThinFace,
            onValueChanged = { value ->
                beautyConfig.naturalThinFace = value
            }
        ),

    )

private fun getLittleFaceShapeList(beautyConfig: SenseTimeBeautySDK.BeautyConfig): List<BaseControllerView.ItemInfo> =
    listOf(
        BaseControllerView.ItemInfo(
            R.string.show_beauty_item_none,
            R.mipmap.show_beauty_ic_none,
            isSelected = !SenseTimeBeautySDK.beautyConfigCache.littleFaceShapeEnable,
            onValueChanged = { _ ->
                SenseTimeBeautySDK.clearLittleShape()
            }
        ),
        BaseControllerView.ItemInfo(
            R.string.show_beauty_item_beauty_headsscale,
            R.drawable.svg_small_head,
            beautyConfig.smallHead,
            isSelected = SenseTimeBeautySDK.beautyConfigCache.littleFaceShapeEnable,
            onValueChanged = { value ->
                beautyConfig.smallHead = value
            }
        ),
        BaseControllerView.ItemInfo(
            R.string.show_beauty_item_beauty_thine_face,
            R.mipmap.show_beauty_ic_face_shoulian,
            beautyConfig.thinFace,
            onValueChanged = { value ->
                beautyConfig.thinFace = value
            }
        ),

        BaseControllerView.ItemInfo(
            R.string.show_beauty_item_beauty_vChin,
            R.mipmap.show_beauty_ic_face_xiaba,
            beautyConfig.vChin,
            onValueChanged = { value ->
                beautyConfig.vChin = value
            }
        ),

        BaseControllerView.ItemInfo(
            R.string.show_beauty_item_beauty_vFace,
            R.drawable.svg_narrow_face,
            beautyConfig.vFace,
            onValueChanged = { value ->
                beautyConfig.vFace = value
            }
        ),

        BaseControllerView.ItemInfo(
            R.string.show_beauty_item_beauty_chin,
            R.mipmap.show_beauty_ic_face_xiaba,
            SenseTimeBeautySDK.beautyConfig.chinLength,
            onValueChanged = { value ->
                SenseTimeBeautySDK.beautyConfig.chinLength = value
            }
        ),
        BaseControllerView.ItemInfo(
            R.string.show_beauty_item_beauty_jawbone,
            R.mipmap.show_beauty_ic_face_xiahegu,
            SenseTimeBeautySDK.beautyConfig.shrinkJawbone,
            onValueChanged = { value ->
                SenseTimeBeautySDK.beautyConfig.shrinkJawbone = value
            }
        ),
        BaseControllerView.ItemInfo(
            R.string.show_beauty_item_beauty_cheekbone,
            R.mipmap.show_beauty_ic_face_shouquangu,
            SenseTimeBeautySDK.beautyConfig.shrinkCheekbone,
            onValueChanged = { value ->
                SenseTimeBeautySDK.beautyConfig.shrinkCheekbone = value
            }
        ),
        BaseControllerView.ItemInfo(
            R.string.show_beauty_item_beauty_nose,
            R.mipmap.show_beauty_ic_face_shoubi,
            SenseTimeBeautySDK.beautyConfig.narrowNose,
            onValueChanged = { value ->
                SenseTimeBeautySDK.beautyConfig.narrowNose = value
            }
        ),
        BaseControllerView.ItemInfo(
            R.string.show_beauty_item_beauty_nose_tip,
            R.mipmap.show_beauty_ic_face_shoubi,
            SenseTimeBeautySDK.beautyConfig.noseTip,
            onValueChanged = { value ->
                SenseTimeBeautySDK.beautyConfig.noseTip = value
            }
        ),
        BaseControllerView.ItemInfo(
            R.string.show_beauty_item_beauty_nose_long,
            R.mipmap.show_beauty_ic_face_shoubi,
            SenseTimeBeautySDK.beautyConfig.noseLength,
            onValueChanged = { value ->
                SenseTimeBeautySDK.beautyConfig.noseLength = value
            }
        ),

        BaseControllerView.ItemInfo(
            R.string.show_beauty_item_beauty_open_canthus,
            R.mipmap.show_beauty_ic_face_eye,
            SenseTimeBeautySDK.beautyConfig.openCanthus,
            onValueChanged = { value ->
                SenseTimeBeautySDK.beautyConfig.openCanthus = value
            }
        ),

        BaseControllerView.ItemInfo(
            R.string.show_beauty_item_beauty_open_external_canthus,
            R.mipmap.show_beauty_ic_face_eye,
            SenseTimeBeautySDK.beautyConfig.openExternalCanthus,
            onValueChanged = { value ->
                SenseTimeBeautySDK.beautyConfig.openExternalCanthus = value
            }
        ),

        BaseControllerView.ItemInfo(
            R.string.show_beauty_item_beauty_bright_eye,
            R.mipmap.show_beauty_ic_face_bright_eye,
            SenseTimeBeautySDK.beautyConfig.brightEye,
            onValueChanged = { value ->
                SenseTimeBeautySDK.beautyConfig.brightEye = value
            }
        ),
        BaseControllerView.ItemInfo(
            R.string.show_beauty_item_beauty_remove_dark_circles,
            R.mipmap.show_beauty_ic_face_remove_dark_circles,
            SenseTimeBeautySDK.beautyConfig.darkCircles,
            onValueChanged = { value ->
                SenseTimeBeautySDK.beautyConfig.darkCircles = value
            }
        ),
        BaseControllerView.ItemInfo(
            R.string.show_beauty_item_beauty_remove_nasolabial_folds,
            R.mipmap.show_beauty_ic_face_remove_nasolabial_folds,
            SenseTimeBeautySDK.beautyConfig.nasolabialFolds,
            onValueChanged = { value ->
                SenseTimeBeautySDK.beautyConfig.nasolabialFolds = value
            }
        ),
        BaseControllerView.ItemInfo(
            R.string.show_beauty_item_beauty_teeth,
            R.mipmap.show_beauty_ic_face_meiya,
            SenseTimeBeautySDK.beautyConfig.whiteTeeth,
            onValueChanged = { value ->
                SenseTimeBeautySDK.beautyConfig.whiteTeeth = value
            }
        ),  BaseControllerView.ItemInfo(
            R.string.show_beauty_item_beauty_forehead,
            R.mipmap.show_beauty_ic_face_etou,
            SenseTimeBeautySDK.beautyConfig.hairlineHeight,
            onValueChanged = { value ->
                SenseTimeBeautySDK.beautyConfig.hairlineHeight = value
            }
        ), BaseControllerView.ItemInfo(
            R.string.show_beauty_item_beauty_mouth,
            R.mipmap.show_beauty_ic_face_zuixing,
            SenseTimeBeautySDK.beautyConfig.mouthSize,
            onValueChanged = { value ->
                SenseTimeBeautySDK.beautyConfig.mouthSize = value
            }
        )
    )

private fun getGroupAdjustList(): List<BaseControllerView.ItemInfo> = listOf(
    BaseControllerView.ItemInfo(
        R.string.show_beauty_item_none,
        R.mipmap.show_beauty_ic_none,
        0.0f,
        isSelected = SenseTimeBeautySDK.isClearGroupAdjust(),
        onValueChanged = { _ ->
            SenseTimeBeautySDK.clearGroupAdjust()
        },
    ),
    BaseControllerView.ItemInfo(
        R.string.show_beauty_item_adjust_contrast,
        R.mipmap.show_beauty_ic_adjust_contrast,
        SenseTimeBeautySDK.beautyConfig.contrast,
        isSelected = !SenseTimeBeautySDK.isClearGroupAdjust(),
        onValueChanged = { value ->
            SenseTimeBeautySDK.beautyConfig.contrast = value
        }
    ),
    BaseControllerView.ItemInfo(
        R.string.show_beauty_item_adjust_saturation,
        R.mipmap.show_beauty_ic_adjust_saturation,
        SenseTimeBeautySDK.beautyConfig.saturation,
        onValueChanged = { value ->
            SenseTimeBeautySDK.beautyConfig.saturation = value
        }
    ),
    BaseControllerView.ItemInfo(
        R.string.show_beauty_item_adjust_sharpen,
        R.mipmap.show_beauty_ic_adjust_sharp,
        SenseTimeBeautySDK.beautyConfig.sharpen,
        onValueChanged = { value ->
            SenseTimeBeautySDK.beautyConfig.sharpen = value
        }
    ),
    BaseControllerView.ItemInfo(
        R.string.show_beauty_item_adjust_clarity,
        R.mipmap.show_beauty_ic_adjust_clear,
        SenseTimeBeautySDK.beautyConfig.clear,
        onValueChanged = { value ->
            SenseTimeBeautySDK.beautyConfig.clear = value
        }
    ),
)