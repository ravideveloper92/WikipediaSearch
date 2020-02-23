package com.wikipedia.suggestededits

import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.CompoundButton
import androidx.appcompat.app.AlertDialog
import com.google.android.material.chip.Chip
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_suggested_edits_image_tags_item.*
import com.wikipedia.Constants
import com.wikipedia.R
import com.wikipedia.WikipediaApp
import com.wikipedia.csrf.CsrfTokenClient
import com.wikipedia.dataclient.Service
import com.wikipedia.dataclient.ServiceFactory
import com.wikipedia.dataclient.WikiSite
import com.wikipedia.dataclient.mwapi.MwPostResponse
import com.wikipedia.dataclient.mwapi.MwQueryPage
import com.wikipedia.dataclient.mwapi.media.MediaHelper
import com.wikipedia.dataclient.wikidata.EntityPostResponse
import com.wikipedia.login.LoginClient.LoginFailedException
import com.wikipedia.page.LinkMovementMethodExt
import com.wikipedia.settings.Prefs
import com.wikipedia.suggestededits.provider.MissingDescriptionProvider
import com.wikipedia.util.*
import com.wikipedia.util.L10nUtil.setConditionalLayoutDirection
import com.wikipedia.util.log.L
import com.wikipedia.views.ImageZoomHelper
import java.util.*
import kotlin.collections.ArrayList

class SuggestedEditsImageTagsFragment : SuggestedEditsItemFragment(), CompoundButton.OnCheckedChangeListener {
    var publishing: Boolean = false
    var publishSuccess: Boolean = false
    private var csrfClient: com.wikipedia.csrf.CsrfTokenClient = com.wikipedia.csrf.CsrfTokenClient(WikiSite(com.wikipedia.dataclient.Service.COMMONS_URL))
    private var page: com.wikipedia.dataclient.mwapi.MwQueryPage? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_suggested_edits_image_tags_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setConditionalLayoutDirection(contentContainer, parent().langFromCode)
        imageView.setLegacyVisibilityHandlingEnabled(true)
        cardItemErrorView.setBackClickListener { requireActivity().finish() }
        cardItemErrorView.setRetryClickListener {
            cardItemProgressBar.visibility = VISIBLE
            cardItemErrorView.visibility = GONE
            getNextItem()
        }

        val transparency = 0xcc000000
        tagsContainer.setBackgroundColor(transparency.toInt() or (com.wikipedia.util.ResourceUtil.getThemedColor(requireContext(), R.attr.paper_color) and 0xffffff))
        imageCaption.setBackgroundColor(transparency.toInt() or (com.wikipedia.util.ResourceUtil.getThemedColor(requireContext(), R.attr.paper_color) and 0xffffff))

        publishOverlayContainer.setBackgroundColor(transparency.toInt() or (com.wikipedia.util.ResourceUtil.getThemedColor(requireContext(), R.attr.paper_color) and 0xffffff))
        publishOverlayContainer.visibility = GONE

        val colorStateList = ColorStateList(arrayOf(intArrayOf()),
                intArrayOf(if (com.wikipedia.WikipediaApp.getInstance().currentTheme.isDark) Color.WHITE else com.wikipedia.util.ResourceUtil.getThemedColor(requireContext(), R.attr.colorAccent)))
        publishProgressBar.progressTintList = colorStateList
        publishProgressCheck.imageTintList = colorStateList
        publishProgressText.setTextColor(colorStateList)

        tagsLicenseText.text = com.wikipedia.util.StringUtil.fromHtml(getString(R.string.suggested_edits_cc0_notice,
                getString(R.string.terms_of_use_url), getString(R.string.cc_0_url)))
        tagsLicenseText.movementMethod = com.wikipedia.page.LinkMovementMethodExt.getInstance()

        getNextItem()
        updateContents()
    }

    override fun onStart() {
        super.onStart()
        parent().updateActionButton()
    }

    private fun getNextItem() {
        if (page != null) {
            return
        }
        disposables.add(MissingDescriptionProvider.getNextImageWithMissingTags(parent().langFromCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ page ->
                    this.page = page
                    updateContents()
                }, { this.setErrorState(it) })!!)
    }

    private fun setErrorState(t: Throwable) {
        com.wikipedia.util.log.L.e(t)
        cardItemErrorView.setError(t)
        cardItemErrorView.visibility = VISIBLE
        cardItemProgressBar.visibility = GONE
        contentContainer.visibility = GONE
    }

    private fun updateContents() {
        cardItemErrorView.visibility = GONE
        contentContainer.visibility = if (page != null) VISIBLE else GONE
        cardItemProgressBar.visibility = if (page != null) GONE else VISIBLE
        if (page == null) {
            return
        }

        tagsLicenseText.visibility = GONE
        tagsHintText.visibility = VISIBLE
        com.wikipedia.views.ImageZoomHelper.setViewZoomable(imageView)

        imageView.loadImage(Uri.parse(ImageUrlUtil.getUrlForPreferredSize(page!!.imageInfo()!!.thumbUrl, com.wikipedia.Constants.PREFERRED_CARD_THUMBNAIL_SIZE)))

        val typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        tagsChipGroup.removeAllViews()
        val maxTags = 5
        for (label in page!!.imageLabels) {
            if (label.state != "unreviewed") {
                continue
            }
            val chip = Chip(requireContext())
            chip.text = label.label
            chip.textAlignment = TEXT_ALIGNMENT_CENTER
            chip.setChipBackgroundColorResource(com.wikipedia.util.ResourceUtil.getThemedAttributeId(requireContext(), R.attr.chip_background_color))
            chip.chipStrokeWidth = com.wikipedia.util.DimenUtil.dpToPx(1f)
            chip.setChipStrokeColorResource(com.wikipedia.util.ResourceUtil.getThemedAttributeId(requireContext(), R.attr.chip_background_color))
            chip.setTextColor(com.wikipedia.util.ResourceUtil.getThemedColor(requireContext(), R.attr.material_theme_primary_color))
            chip.typeface = typeface
            chip.isCheckable = true
            chip.setChipIconResource(R.drawable.ic_chip_add_24px)
            chip.chipIconSize = com.wikipedia.util.DimenUtil.dpToPx(24f)
            chip.iconEndPadding = 0f
            chip.textStartPadding = com.wikipedia.util.DimenUtil.dpToPx(2f)
            chip.chipIconTint = ColorStateList.valueOf(com.wikipedia.util.ResourceUtil.getThemedColor(requireContext(), R.attr.material_theme_de_emphasised_color))
            chip.setCheckedIconResource(R.drawable.ic_chip_check_24px)
            chip.setOnCheckedChangeListener(this)
            chip.tag = label

            // add some padding to the Chip, since our container view doesn't support item spacing yet.
            val params = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            val margin = com.wikipedia.util.DimenUtil.roundedDpToPx(8f)
            params.setMargins(margin, 0, margin, 0)
            chip.layoutParams = params

            tagsChipGroup.addView(chip)
            if (tagsChipGroup.childCount >= maxTags) {
                break
            }
        }

        disposables.add(MediaHelper.getImageCaptions(page!!.title())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { captions ->
                    if (captions.containsKey(parent().langFromCode)) {
                        imageCaption.text = captions[parent().langFromCode]
                        imageCaption.visibility = VISIBLE
                    } else {
                        if (page!!.imageInfo() != null && page!!.imageInfo()!!.metadata != null) {
                            imageCaption.text = com.wikipedia.util.StringUtil.fromHtml(page!!.imageInfo()!!.metadata!!.imageDescription()).toString().trim()
                            imageCaption.visibility = VISIBLE
                        } else {
                            imageCaption.visibility = GONE
                        }
                    }
                })

        updateLicenseTextShown()
        parent().updateActionButton()
    }

    companion object {
        fun newInstance(): SuggestedEditsItemFragment {
            return SuggestedEditsImageTagsFragment()
        }
    }

    override fun onCheckedChanged(button: CompoundButton?, isChecked: Boolean) {
        val chip = button as Chip
        if (chip.isChecked) {
            chip.setChipBackgroundColorResource(com.wikipedia.util.ResourceUtil.getThemedAttributeId(requireContext(), R.attr.color_group_55))
            chip.setChipStrokeColorResource(com.wikipedia.util.ResourceUtil.getThemedAttributeId(requireContext(), R.attr.color_group_56))
            chip.isChipIconVisible = false
        } else {
            chip.setChipBackgroundColorResource(com.wikipedia.util.ResourceUtil.getThemedAttributeId(requireContext(), R.attr.chip_background_color))
            chip.setChipStrokeColorResource(com.wikipedia.util.ResourceUtil.getThemedAttributeId(requireContext(), R.attr.chip_background_color))
            chip.isChipIconVisible = true
        }

        updateLicenseTextShown()
        parent().updateActionButton()
    }

    override fun publish() {
        if (publishing || publishSuccess || tagsChipGroup.childCount == 0) {
            return
        }
        var acceptedCount = 0
        for (i in 0 until tagsChipGroup.childCount) {
            val chip = tagsChipGroup.getChildAt(i) as Chip
            if (chip.isChecked) {
                acceptedCount++
            }
        }
        if (acceptedCount == 0) {
            AlertDialog.Builder(requireContext())
                    .setTitle(R.string.suggested_edits_image_tags_select_title)
                    .setMessage(R.string.suggested_edits_image_tags_select_text)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(R.string.description_edit_save) { _, _ ->
                        doPublish()
                    }
                    .show()
            return
        } else {
            doPublish()
        }
    }

    private fun doPublish() {
        val acceptedLabels = ArrayList<String>()
        var rejectedCount = 0
        val batchBuilder = StringBuilder()
        batchBuilder.append("[")
        for (i in 0 until tagsChipGroup.childCount) {
            if (acceptedLabels.size > 0 || rejectedCount > 0) {
                batchBuilder.append(",")
            }
            val chip = tagsChipGroup.getChildAt(i) as Chip
            val label = chip.tag as com.wikipedia.dataclient.mwapi.MwQueryPage.ImageLabel
            batchBuilder.append("{\"label\":\"")
            batchBuilder.append(label.wikidataId)
            batchBuilder.append("\",\"review\":\"")
            batchBuilder.append(if (chip.isChecked) "accept" else "reject")
            batchBuilder.append("\"}")
            if (chip.isChecked) {
                acceptedLabels.add(label.wikidataId)
            } else {
                rejectedCount++
            }
        }
        batchBuilder.append("]")

        // -- point of no return --

        publishing = true
        publishSuccess = false

        publishProgressText.setText(R.string.suggested_edits_image_tags_publishing)
        publishProgressCheck.visibility = GONE
        publishOverlayContainer.visibility = VISIBLE

        // kick off the circular animation
        val duration = 2000L
        val animator = ObjectAnimator.ofInt(publishProgressBar, "progress", 0, 1000)
        animator.duration = duration
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()
        publishProgressBar.postDelayed({
            if (isAdded && !publishing && publishSuccess) {
                onSuccess()
            }
        }, duration)

        val commonsSite = com.wikipedia.dataclient.WikiSite(com.wikipedia.dataclient.Service.COMMONS_URL)

        csrfClient.request(false, object : com.wikipedia.csrf.CsrfTokenClient.Callback {
            override fun success(token: String) {

                val claimObservables = ArrayList<ObservableSource<com.wikipedia.dataclient.wikidata.EntityPostResponse>>()
                for (label in acceptedLabels) {
                    val claimTemplate = "{\"mainsnak\":" +
                            "{\"snaktype\":\"value\",\"property\":\"P180\"," +
                            "\"datavalue\":{\"value\":" +
                            "{\"entity-type\":\"item\",\"id\":\"${label}\"}," +
                            "\"type\":\"wikibase-entityid\"},\"datatype\":\"wikibase-item\"}," +
                            "\"type\":\"statement\"," +
                            "\"id\":\"M${page!!.pageId()}\$${UUID.randomUUID()}\"," +
                            "\"rank\":\"normal\"}"

                    claimObservables.add(com.wikipedia.dataclient.ServiceFactory.get(commonsSite).postSetClaim(claimTemplate, token, null, null))
                }

                disposables.add(com.wikipedia.dataclient.ServiceFactory.get(commonsSite).postReviewImageLabels(page!!.title(), token, batchBuilder.toString())
                        .flatMap { response ->
                            if (claimObservables.size > 0) {
                                Observable.zip(claimObservables) { responses ->
                                    responses[0]
                                }
                            } else {
                                Observable.just(response)
                            }
                        }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doAfterTerminate {
                            publishing = false
                        }
                        .subscribe({ response ->
                            // TODO: check anything else in the response?
                            publishSuccess = true
                            if (!animator.isRunning) {
                                // if the animator is still running, let it finish and invoke success() on its own
                                onSuccess()
                            }
                        }, { caught ->
                            onError(caught)
                        }))
            }

            override fun failure(caught: Throwable) {
                onError(caught)
            }

            override fun twoFactorPrompt() {
                onError(LoginFailedException(resources.getString(R.string.login_2fa_other_workflow_error_msg)))
            }
        })
    }

    private fun onSuccess() {
        publishProgressText.setText(R.string.suggested_edits_image_tags_published)

        com.wikipedia.settings.Prefs.setSuggestedEditsImageTagsNew(false)

        playSuccessVibration()

        val duration = 500L
        publishProgressCheck.alpha = 0f
        publishProgressCheck.visibility = VISIBLE
        publishProgressCheck.animate()
                .alpha(1f)
                .duration = duration

        publishProgressBar.postDelayed({
            if (isAdded) {
                
                for (i in 0 until tagsChipGroup.childCount) {
                    val chip = tagsChipGroup.getChildAt(i) as Chip
                    chip.isEnabled = false
                }
                updateLicenseTextShown()

                publishOverlayContainer.visibility = GONE
                parent().nextPage()
                setPublishedState()
            }
        }, duration * 3)
    }

    private fun onError(caught: Throwable) {
        // TODO: expand this a bit.
        publishOverlayContainer.visibility = GONE
        com.wikipedia.util.FeedbackUtil.showError(requireActivity(), caught)
    }

    private fun setPublishedState() {
        for (i in 0 until tagsChipGroup.childCount) {
            val chip = tagsChipGroup.getChildAt(i) as Chip
            if (chip.isChecked) {
                chip.setChipBackgroundColorResource(com.wikipedia.util.ResourceUtil.getThemedAttributeId(requireContext(), R.attr.color_group_57))
                chip.setChipStrokeColorResource(com.wikipedia.util.ResourceUtil.getThemedAttributeId(requireContext(), R.attr.color_group_58))
            } else {
                chip.setChipBackgroundColorResource(com.wikipedia.util.ResourceUtil.getThemedAttributeId(requireContext(), R.attr.chip_background_color))
                chip.setChipStrokeColorResource(com.wikipedia.util.ResourceUtil.getThemedAttributeId(requireContext(), R.attr.chip_background_color))
            }
        }
    }

    private fun playSuccessVibration() {
        val v = requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val pattern = longArrayOf(0, 100)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            v.vibrate(pattern, -1)
        }
    }

    private fun updateLicenseTextShown() {
        if (publishSuccess) {
            tagsLicenseText.visibility = GONE
            tagsHintText.visibility = GONE
        } else if (atLeastOneTagChecked()) {
            tagsLicenseText.visibility = VISIBLE
            tagsHintText.visibility = GONE
        } else {
            tagsLicenseText.visibility = GONE
            tagsHintText.visibility = VISIBLE
        }
    }

    private fun atLeastOneTagChecked(): Boolean {
        var atLeastOneChecked = false
        for (i in 0 until tagsChipGroup.childCount) {
            val chip = tagsChipGroup.getChildAt(i) as Chip
            if (chip.isChecked) {
                atLeastOneChecked = true
                break
            }
        }
        return atLeastOneChecked
    }

    override fun publishEnabled(): Boolean {
        return !publishSuccess
    }

    override fun publishOutlined(): Boolean {
        if (tagsChipGroup == null) {
            return false
        }
        return !atLeastOneTagChecked()
    }
}
