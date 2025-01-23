package cn.yanhu.baselib.adapter

import android.content.Context
import android.util.SparseArray
import android.view.ViewGroup
import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter
import java.lang.ref.WeakReference
import java.util.Collections
import com.chad.library.adapter4.R

/**
 * @author: zhengjun
 * created: 2025/1/14
 * desc:
 */
open class BaseMultiItemDifferAdapter<T : Any>( config: AsyncDifferConfig<T>, items: List<T> = emptyList()) :
    BaseQuickAdapter<T, RecyclerView.ViewHolder>(items) {

    private val typeViewHolders =
        SparseArray<OnMultiItemAdapterListener<T, RecyclerView.ViewHolder>>(1)

    private var onItemViewTypeListener: OnItemViewTypeListener<T>? = null

    private val mDiffer: AsyncListDiffer<T> =
        AsyncListDiffer(AdapterListUpdateCallback(this), config)

    constructor(diffCallback: DiffUtil.ItemCallback<T>) : this(diffCallback, emptyList())

    constructor(diffCallback: DiffUtil.ItemCallback<T>, items: List<T>) : this(
        AsyncDifferConfig.Builder(diffCallback).build(), items
    )

    constructor(config: AsyncDifferConfig<T>) : this(config, emptyList())

    private val mListener: AsyncListDiffer.ListListener<T> =
        AsyncListDiffer.ListListener<T> { previousList, currentList ->

            val oldDisplayEmptyLayout = displayEmptyView(previousList)
            val newDisplayEmptyLayout = displayEmptyView(currentList)

            if (oldDisplayEmptyLayout && !newDisplayEmptyLayout) {
                notifyItemRemoved(0)
                recyclerView.scrollToPosition(0)
            } else if (newDisplayEmptyLayout && !oldDisplayEmptyLayout) {
                notifyItemInserted(0)
            } else if (oldDisplayEmptyLayout && newDisplayEmptyLayout) {
                notifyItemChanged(0, 0)
            }

            this.onCurrentListChanged(previousList, currentList)
        }

    init {
        mDiffer.addListListener(mListener)
        mDiffer.submitList(items)
    }

    /**
     * item 数据集
     */
    final override var items: List<T>
        get() = mDiffer.currentList
        set(value) {
            mDiffer.submitList(value, null)
        }

    /**
     * (Optional) Override this method to monitor changes in the dataset before and after..
     * （可选）重写此方法，监听前后数据集变化
     *
     * @param previousList 原数据集
     * @param currentList 当前数据集
     */
    open fun onCurrentListChanged(previousList: List<T>, currentList: List<T>) {}

    /**
     * Submit List
     * 提交数据集
     *
     * @param list
     */
    override fun submitList(list: List<T>?) {
        mDiffer.submitList(list, null)
    }

    /**
     * Submit list
     * 提交数据集
     *
     * @param list
     * @param commitCallback 数据异步提交完成以后的回掉
     */
    fun submitList(list: List<T>?, commitCallback: Runnable?) {
        mDiffer.submitList(list, commitCallback)
    }

    override operator fun set(position: Int, data: T) {
        set(position, data, null)
    }

    override fun add(data: T) {
        add(data, null)
    }

    override fun add(position: Int, data: T) {
        add(position, data, null)
    }

    override fun addAll(collection: Collection<T>) {
        addAll(collection, null)
    }

    override fun addAll(position: Int, collection: Collection<T>) {
        addAll(position, collection, null)
    }

    override fun removeAt(position: Int) {
        removeAt(position, null)
    }

    override fun remove(data: T) {
        remove(data, null)
    }

    override fun removeAtRange(range: IntRange) {
        removeAtRange(range, null)
    }

    override fun swap(fromPosition: Int, toPosition: Int) {
        swap(fromPosition, toPosition, null)
    }

    override fun move(fromPosition: Int, toPosition: Int) {
        move(fromPosition, toPosition, null)
    }


    /**
     * 带 Runnable
     */
    open fun set(position: Int, data: T, commitCallback: Runnable?) {
        items.toMutableList().also {
            it[position] = data
            submitList(it, commitCallback)
        }
    }

    open fun add(data: T, commitCallback: Runnable?) {
        items.toMutableList().also {
            it.add(data)
            submitList(it, commitCallback)
        }
    }

    open fun add(position: Int, data: T, commitCallback: Runnable?) {
        if (position > items.size || position < 0) {
            throw IndexOutOfBoundsException("position: ${position}. size:${items.size}")
        }

        items.toMutableList().also {
            it.add(position, data)
            submitList(it, commitCallback)
        }
    }

    open fun addAll(collection: Collection<T>, commitCallback: Runnable?) {
        items.toMutableList().also {
            it.addAll(collection)
            submitList(it, commitCallback)
        }
    }

    open fun addAll(position: Int, collection: Collection<T>, commitCallback: Runnable?) {
        if (position > items.size || position < 0) {
            throw IndexOutOfBoundsException("position: ${position}. size:${items.size}")
        }

        items.toMutableList().also {
            it.addAll(position, collection)
            submitList(it, commitCallback)
        }
    }

    open fun removeAt(position: Int, commitCallback: Runnable?) {
        if (position >= items.size) {
            throw IndexOutOfBoundsException("position: ${position}. size:${items.size}")
        }

        items.toMutableList().also {
            it.removeAt(position)
            submitList(it, commitCallback)
        }
    }

    open fun remove(data: T, commitCallback: Runnable?) {
        items.toMutableList().also {
            it.remove(data)
            submitList(it, commitCallback)
        }
    }

    open fun removeAtRange(range: IntRange, commitCallback: Runnable?) {
        if (range.isEmpty()) {
            return
        }
        if (range.first >= items.size) {
            throw IndexOutOfBoundsException("Range first position: ${range.first} - last position: ${range.last}. size:${items.size}")
        }

        val last = if (range.last >= items.size) {
            items.size - 1
        } else {
            range.last
        }

        val list = items.toMutableList()
        for (it in last downTo  range.first) {
            list.removeAt(it)
        }
        submitList(list, commitCallback)
    }

    open fun swap(fromPosition: Int, toPosition: Int, commitCallback: Runnable?) {
        if (fromPosition in items.indices || toPosition in items.indices) {
            items.toMutableList().also {
                Collections.swap(it, fromPosition, toPosition)
                submitList(it, commitCallback)
            }
        }
    }

    open fun move(fromPosition: Int, toPosition: Int, commitCallback: Runnable?) {
        if (fromPosition in items.indices || toPosition in items.indices) {
            items.toMutableList().also {
                val e = it.removeAt(fromPosition)
                it.add(toPosition, e)
                submitList(it, commitCallback)
            }
        }
    }

    override fun onCreateViewHolder(
        context: Context, parent: ViewGroup, viewType: Int
    ): RecyclerView.ViewHolder {
        val listener = typeViewHolders.get(viewType)
            ?: throw IllegalArgumentException("ViewType: $viewType not found onViewHolderListener，please use addItemType() first!")

        return listener.onCreate(parent.context, parent, viewType).apply {
            itemView.setTag(R.id.BaseQuickAdapter_key_multi, listener)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, item: T?) {
        findListener(holder)?.onBind(holder, position, item)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder, position: Int, item: T?, payloads: List<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position, item)
            return
        }

        findListener(holder)?.onBind(holder, position, item, payloads)
    }

    /**
     * Call this function to add multiTypeItems.
     * 调用此方法，设置多布局
     * @param itemViewType Int
     * @param listener Int
     */
    fun <V : RecyclerView.ViewHolder> addItemType(
        itemViewType: Int, listener: OnMultiItemAdapterListener<T, V>
    ) = apply {
        if (listener is OnMultiItem) {
            listener.weakA = WeakReference(this)
        }
        typeViewHolders.put(
            itemViewType, listener as OnMultiItemAdapterListener<T, RecyclerView.ViewHolder>
        )
    }

    /**
     * 设置 ItemViewType 的监听，根据不同数据类型，返回不同的type值
     *
     * @param listener
     */
    fun onItemViewType(listener: OnItemViewTypeListener<T>?) = apply {
        this.onItemViewTypeListener = listener
    }

    override fun getItemViewType(position: Int, list: List<T>): Int {
        return onItemViewTypeListener?.onItemViewType(position, list)
            ?: super.getItemViewType(position, list)
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)

        findListener(holder)?.onViewAttachedToWindow(holder)
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        findListener(holder)?.onViewDetachedFromWindow(holder)
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        findListener(holder)?.onViewRecycled(holder)
    }

    override fun onFailedToRecycleView(holder: RecyclerView.ViewHolder): Boolean {
        return findListener(holder)?.onFailedToRecycleView(holder) ?: false
    }

    override fun isFullSpanItem(itemType: Int): Boolean {
        return super.isFullSpanItem(itemType) ||
                (typeViewHolders.get(itemType)?.isFullSpanItem(itemType) == true)
    }

    private fun findListener(holder: RecyclerView.ViewHolder) : OnMultiItemAdapterListener<T, RecyclerView.ViewHolder>? {
        return holder.itemView.getTag(R.id.BaseQuickAdapter_key_multi) as? OnMultiItemAdapterListener<T, RecyclerView.ViewHolder>
    }

    /**
     * 多类型布局 Adapter Listener
     *
     * @param T 数据类型
     * @param V ViewHolder 类型
     */
    interface OnMultiItemAdapterListener<T, V : RecyclerView.ViewHolder> {
        fun onCreate(context: Context, parent: ViewGroup, viewType: Int): V

        fun onBind(holder: V, position: Int, item: T?)

        fun onBind(holder: V, position: Int, item: T?, payloads: List<Any>) {
            onBind(holder, position, item)
        }

        fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {}

        fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {}

        fun onViewRecycled(holder: RecyclerView.ViewHolder) {}

        fun onFailedToRecycleView(holder: RecyclerView.ViewHolder): Boolean = false

        fun isFullSpanItem(itemType: Int): Boolean {
            return false
        }
    }

    /**
     * 如果需要一些属性，例如：adapter、context，则使用此抽象类.
     * 如果不需要，则可以直接实现[OnMultiItemAdapterListener]接口.
     *
     * @param T
     * @param V
     * @constructor Create empty On multi item
     */
    abstract class OnMultiItem<T : Any, V : RecyclerView.ViewHolder> :
        OnMultiItemAdapterListener<T, V> {
        internal var weakA: WeakReference<BaseMultiItemDifferAdapter<T>>? = null

        val adapter: BaseMultiItemDifferAdapter<T>?
            get() = weakA?.get()

        val context: Context?
            get() = weakA?.get()?.context
    }

    fun interface OnItemViewTypeListener<T> {
        /**
         * 根据不同数据类型，返回不同的type值
         *
         * @param position
         * @param list
         * @return
         */
        fun onItemViewType(position: Int, list: List<T>): Int
    }
}