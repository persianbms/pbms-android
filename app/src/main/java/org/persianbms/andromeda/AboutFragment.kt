package org.persianbms.andromeda

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView

class AboutFragment : Fragment() {

    companion object {
        private const val PBMS_HEADER_ROW_ID = 1L
        private const val SECTION1_GAP_ROW_ID = 2L
        private const val ABOUT_US_ROW_ID = 3L
        private const val CONTACT_US_ROW_ID = 4L
        private const val SECTION2_GAP_ROW_ID = 5L
        private const val SATELLITE_BROADCAST_INFO_ROW_ID = 6L
        private const val FOLLOW_US_ROW_ID = 7L
        private const val TELEGRAM_ROW_ID = 8L
        private const val INSTAGRAM_ROW_ID = 9L
        private const val FACEBOOK_ROW_ID = 10L
        private const val YOUTUBE_ROW_ID = 11L
        private const val SOUNDCLOUD_ROW_ID = 12L
        private const val TWITTER_ROW_ID = 13L

        fun newInstance(): AboutFragment {
            return AboutFragment()
        }
    }

    private val rows = ArrayList<RecyclerAdapterItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        rows.add(RecyclerAdapterItem(PBMS_HEADER_ROW_ID, R.layout.list_item_pbms_header))
        rows.add(RecyclerAdapterItem(SECTION1_GAP_ROW_ID, R.layout.list_item_gap))
        rows.add(RecyclerAdapterItem(ABOUT_US_ROW_ID, R.layout.list_item_single_line))
        rows.add(RecyclerAdapterItem(CONTACT_US_ROW_ID, R.layout.list_item_single_line))
        rows.add(RecyclerAdapterItem(SECTION2_GAP_ROW_ID, R.layout.list_item_gap))
        rows.add(RecyclerAdapterItem(SATELLITE_BROADCAST_INFO_ROW_ID, R.layout.list_item_single_line))
        rows.add(RecyclerAdapterItem(FOLLOW_US_ROW_ID, R.layout.list_item_header))
        rows.add(RecyclerAdapterItem(TELEGRAM_ROW_ID, R.layout.list_item_single_line))
        rows.add(RecyclerAdapterItem(INSTAGRAM_ROW_ID, R.layout.list_item_single_line))
        rows.add(RecyclerAdapterItem(FACEBOOK_ROW_ID, R.layout.list_item_single_line))
        rows.add(RecyclerAdapterItem(YOUTUBE_ROW_ID, R.layout.list_item_single_line))
        rows.add(RecyclerAdapterItem(SOUNDCLOUD_ROW_ID, R.layout.list_item_single_line))
        rows.add(RecyclerAdapterItem(TWITTER_ROW_ID, R.layout.list_item_single_line))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val list = inflater.inflate(R.layout.fragment_about, container, false) as RecyclerView
        val adapter = Adapter()
        list.adapter = adapter

        return list
    }

    private fun onRowClicked(id: Long) {
        val ctx = requireContext()
        when (id) {
            ABOUT_US_ROW_ID -> {
                val aboutPbms = AboutPbmsFragment()
                val fm = fragmentManager ?: return
                val tx = fm.beginTransaction()
                tx.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
                tx.replace(R.id.fragment_container, aboutPbms)
                tx.addToBackStack(null).commit()
            }
            CONTACT_US_ROW_ID -> {
                val fragment = ContactUsFragment()
                val fm = fragmentManager ?: return
                val tx = fm.beginTransaction()
                tx.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
                tx.replace(R.id.fragment_container, fragment)
                tx.addToBackStack(null).commit()
            }
            SATELLITE_BROADCAST_INFO_ROW_ID -> {
                val fragment = SatelliteBroadcastInfoFragment()
                val fm = fragmentManager ?: return
                val tx = fm.beginTransaction()
                tx.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
                tx.replace(R.id.fragment_container, fragment)
                tx.addToBackStack(null).commit()
            }
            TELEGRAM_ROW_ID -> {
                try {
                    Uri.parse("tg://resolve?domain=Persianbms").startViewIntent(ctx)
                } catch (ignore: Throwable) {
                    Uri.parse("https://t.me/Persianbms").startViewIntent(ctx)
                }
            }
            INSTAGRAM_ROW_ID -> {
                Uri.parse("https://www.instagram.com/persianbms").startViewIntent(ctx)
            }
            FACEBOOK_ROW_ID -> {
                Uri.parse("https://www.facebook.com/Persianbms").startViewIntent(ctx)
            }
            YOUTUBE_ROW_ID -> {
                Uri.parse("https://www.youtube.com/persianbms").startViewIntent(ctx)
            }
            SOUNDCLOUD_ROW_ID -> {
                Uri.parse("https://soundcloud.com/Persianbms").startViewIntent(ctx)
            }
            TWITTER_ROW_ID -> {
                Uri.parse("https://twitter.com/Persianbms").startViewIntent(ctx)
            }
        }
    }

    inner class Adapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        init {
            setHasStableIds(true)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val item = rows[position]
            when {
                item.viewType == R.layout.list_item_single_line -> {
                    val h = holder as SingleLineItemViewHolder
                    h.image.visibility = View.INVISIBLE
                    h.image.setImageBitmap(null)
                    h.itemView.setOnClickListener {
                        onRowClicked(item.id)
                    }

                    when (item.id) {
                        ABOUT_US_ROW_ID -> {
                            h.primary.setText(R.string.about_us)
                            h.divider.visibility = View.VISIBLE
                        }
                        CONTACT_US_ROW_ID -> {
                            h.primary.setText(R.string.contact_us)
                            h.divider.visibility = View.GONE
                        }
                        SATELLITE_BROADCAST_INFO_ROW_ID -> {
                            h.primary.setText(R.string.satellite_broadcast_information)
                            h.divider.visibility = View.GONE
                        }
                        TELEGRAM_ROW_ID -> {
                            h.primary.setText(R.string.telegram)
                            h.image.visibility = View.VISIBLE
                            h.image.setImageResource(R.drawable.ic_telegram)
                            h.divider.visibility = View.VISIBLE
                        }
                        INSTAGRAM_ROW_ID -> {
                            h.primary.setText(R.string.instagram)
                            h.image.visibility = View.VISIBLE
                            h.image.setImageResource(R.drawable.ic_instagram)
                            h.divider.visibility = View.VISIBLE
                        }
                        FACEBOOK_ROW_ID -> {
                            h.primary.setText(R.string.facebook)
                            h.image.visibility = View.VISIBLE
                            h.image.setImageResource(R.drawable.ic_facebook)
                            h.divider.visibility = View.VISIBLE
                        }
                        YOUTUBE_ROW_ID -> {
                            h.primary.setText(R.string.youtube)
                            h.image.visibility = View.VISIBLE
                            h.image.setImageResource(R.drawable.ic_youtube)
                            h.divider.visibility = View.VISIBLE
                        }
                        SOUNDCLOUD_ROW_ID -> {
                            h.primary.setText(R.string.soundcloud)
                            h.image.visibility = View.VISIBLE
                            h.image.setImageResource(R.drawable.ic_soundcloud)
                            h.divider.visibility = View.VISIBLE
                        }
                        TWITTER_ROW_ID -> {
                            h.primary.setText(R.string.twitter)
                            h.image.visibility = View.VISIBLE
                            h.image.setImageResource(R.drawable.ic_twitter)
                            h.divider.visibility = View.GONE
                        }
                        else -> {
                            throw RuntimeException("unhandled single line item with id $(item.id)")
                        }
                    }

                    return
                }
                item.viewType == R.layout.list_item_header -> {
                    val h = holder as HeaderViewHolder
                    h.header.setText(R.string.follow_us)
                    return
                }
                item.viewType == R.layout.list_item_gap -> return
                item.viewType == R.layout.list_item_pbms_header -> return
                else -> throw RuntimeException("unknown view type: '${item.viewType}'")
            }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(viewType, parent, false)
            return when (viewType) {
                R.layout.list_item_single_line -> SingleLineItemViewHolder(view)
                R.layout.list_item_header -> HeaderViewHolder(view)
                R.layout.list_item_gap -> NopHolder(view)
                R.layout.list_item_pbms_header -> NopHolder(view)
                else -> throw RuntimeException("unknown view type: $viewType")
            }
        }

        override fun getItemCount(): Int {
            return rows.size
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItemViewType(position: Int): Int {
            return rows[position].viewType
        }

    }
}
