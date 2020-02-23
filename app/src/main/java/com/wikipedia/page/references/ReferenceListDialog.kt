package com.wikipedia.page.references

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.dialog_reference_list.*
import com.wikipedia.R
import com.wikipedia.activity.FragmentUtil
import com.wikipedia.page.ExtendedBottomSheetDialogFragment
import com.wikipedia.page.LinkHandler
import com.wikipedia.page.LinkMovementMethodExt
import com.wikipedia.util.DimenUtil
import com.wikipedia.util.L10nUtil
import com.wikipedia.util.StringUtil
import com.wikipedia.util.log.L
import com.wikipedia.views.DrawableItemDecoration

class ReferenceListDialog : com.wikipedia.page.ExtendedBottomSheetDialogFragment() {
    interface Callback {
        val references: Observable<com.wikipedia.page.references.References>
        val linkHandler: com.wikipedia.page.LinkHandler
    }

    private val disposables = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_reference_list, container)
    }

    override fun onStart() {
        super.onStart()
        BottomSheetBehavior.from(view!!.parent as View).peekHeight = com.wikipedia.util.DimenUtil
                .roundedDpToPx(com.wikipedia.util.DimenUtil.getDimension(R.dimen.readingListSheetPeekHeight))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        com.wikipedia.util.L10nUtil.setConditionalLayoutDirection(view, callback()!!.linkHandler.wikiSite.languageCode())
        referencesRecycler.layoutManager = LinearLayoutManager(activity)
        referencesRecycler.addItemDecoration(DrawableItemDecoration(requireContext(), R.attr.list_separator_drawable, false, false))
        updateList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposables.clear()
    }

    private fun updateList() {
        disposables.add(callback()!!.references
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ references: com.wikipedia.page.references.References -> referencesRecycler.adapter = ReferenceListAdapter(references) }) { t: Throwable? -> com.wikipedia.util.log.L.d(t) })
    }

    private inner class ReferenceListItemHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val referenceTextView: TextView = itemView.findViewById(R.id.reference_text)
        private val referenceIdView: TextView = itemView.findViewById(R.id.reference_id_text)
        private val referenceBackLink: View = itemView.findViewById(R.id.reference_back_link)
        init {
            referenceTextView.movementMethod = com.wikipedia.page.LinkMovementMethodExt(callback()!!.linkHandler)
            referenceBackLink.setOnClickListener(this)
        }

        fun bindItem(reference: com.wikipedia.page.references.References.Reference, position: Int) {
            referenceIdView.text = ((position + 1).toString() + ".")
            referenceTextView.text = com.wikipedia.util.StringUtil.fromHtml(com.wikipedia.util.StringUtil.removeCiteMarkup(com.wikipedia.util.StringUtil.removeStyleTags(reference.content)))
            if (reference.backLinks.isEmpty()) {
                referenceBackLink.visibility = View.GONE
            } else {
                referenceBackLink.visibility = View.VISIBLE
                referenceBackLink.tag = reference.backLinks[0].href
            }
        }

        override fun onClick(v: View?) {
            val href = v!!.tag as String
            callback()!!.linkHandler.onPageLinkClicked(href, "")
        }
    }

    private inner class ReferenceListAdapter(private val references: com.wikipedia.page.references.References) : RecyclerView.Adapter<ReferenceListItemHolder>() {
        private val referenceKeys: Array<String> = references.referencesMap.keys.toTypedArray()

        override fun getItemCount(): Int {
            return referenceKeys.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, pos: Int): ReferenceListItemHolder {
            val view = layoutInflater.inflate(R.layout.item_reference, null)
            val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            view.layoutParams = params
            return ReferenceListItemHolder(view)
        }

        override fun onBindViewHolder(holder: ReferenceListItemHolder, pos: Int) {
            holder.bindItem(references.referencesMap[referenceKeys[pos]]!!, pos)
        }
    }

    private fun callback(): Callback? {
        return com.wikipedia.activity.FragmentUtil.getCallback(this, Callback::class.java)
    }

    companion object {
        fun newInstance(): ReferenceListDialog {
            return ReferenceListDialog()
        }
    }
}