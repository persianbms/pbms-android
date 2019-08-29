package org.persianbms.andromeda

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import java.lang.RuntimeException

class ContactUsFragment : Fragment() {

    companion object {
        private const val PAGE_HEADER_ROW_ID = 1L
        private const val MESSAGE_US_ROW_ID = 2L
        private const val EMAIL_US_ROW_ID = 3L
        private const val CALL_US_ROW_ID = 4L
        private const val TELEGRAM_ROW_ID = 5L
    }

    private val rows = ArrayList<RecyclerAdapterItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        rows.add(RecyclerAdapterItem(PAGE_HEADER_ROW_ID, R.layout.list_item_header))
        rows.add(RecyclerAdapterItem(MESSAGE_US_ROW_ID, R.layout.list_item_two_line))
        rows.add(RecyclerAdapterItem(EMAIL_US_ROW_ID, R.layout.list_item_two_line))
        rows.add(RecyclerAdapterItem(CALL_US_ROW_ID, R.layout.list_item_two_line))
        rows.add(RecyclerAdapterItem(TELEGRAM_ROW_ID, R.layout.list_item_single_line))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val list = inflater.inflate(R.layout.fragment_contact_us, container, false) as RecyclerView
        list.adapter = Adapter()
        return list
    }

    private fun onRowClicked(id: Long) {
        val ctx = requireContext()
        when (id) {
            MESSAGE_US_ROW_ID -> {
                val uri = Uri.parse("https://persianbahaimedia.org/%d8%aa%d9%85%d8%a7%d8%b3-%d8%a8%d8%a7-%d9%85%d8%a7/")
                uri.startViewIntent(ctx)
            }
            EMAIL_US_ROW_ID -> {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:")
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("info@persianbms.org"))
                }
                if (intent.resolveActivity(ctx.packageManager) != null) {
                    startActivity(intent)
                }
            }
            CALL_US_ROW_ID -> {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    val phoneNumber = "+17036718888"
                    data = Uri.parse("tel:$phoneNumber")
                }
                if (intent.resolveActivity(ctx.packageManager) != null) {
                    startActivity(intent)
                }
            }
            TELEGRAM_ROW_ID -> {
                try {
                    Uri.parse("tg://resolve?domain=PersianBMSContact").startViewIntent(ctx)
                } catch (ignore: Throwable) {
                    Uri.parse("https://t.me/PersianBMSContact").startViewIntent(ctx)
                }
            }
        }
    }

    inner class Adapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        init {
            setHasStableIds(true)
        }

        override fun getItemCount(): Int {
            return rows.size
        }

        override fun getItemId(position: Int): Long {
            return rows[position].id
        }

        override fun getItemViewType(position: Int): Int {
            return rows[position].viewType
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val item = rows[position]
            when {
                item.viewType == R.layout.list_item_two_line -> {
                    val h = holder as TwoLineItemViewHolder
                    h.image.visibility = View.VISIBLE
                    h.image.setImageBitmap(null)
                    h.itemView.setOnClickListener {
                        onRowClicked(item.id)
                    }

                    when (item.id) {
                        MESSAGE_US_ROW_ID -> {
                            h.image.setImageResource(R.drawable.ic_outline_message_24dp)
                            h.primary.setText(R.string.message_us)
                            h.secondary.setText(R.string.via_the_web)
                        }
                        EMAIL_US_ROW_ID -> {
                            h.image.setImageResource(R.drawable.ic_outline_email_24dp)
                            h.primary.setText(R.string.email_us)
                            h.secondary.text = "info@persianbms.org"
                        }
                        CALL_US_ROW_ID -> {
                            h.image.setImageResource(R.drawable.ic_outline_phone_24dp)
                            h.primary.setText(R.string.call_us)
                            h.secondary.setText(R.string.pbms_phone_number_msg)
                        }
                    }
                }
                item.viewType == R.layout.list_item_single_line -> {
                    val h = holder as SingleLineItemViewHolder
                    h.image.visibility = View.VISIBLE
                    h.image.setImageResource(R.drawable.ic_telegram)
                    h.primary.setText(R.string.telegram)
                    h.divider.visibility = View.GONE
                    h.itemView.setOnClickListener {
                        onRowClicked(item.id)
                    }

                    return
                }
                item.viewType == R.layout.list_item_header -> {
                    val h = holder as HeaderViewHolder
                    h.header.setText(R.string.contact_us)

                    return
                }
                else -> throw RuntimeException("unknown view type: '${item.viewType}'")
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(viewType, parent, false)
            return when (viewType) {
                R.layout.list_item_two_line -> TwoLineItemViewHolder(view)
                R.layout.list_item_single_line -> SingleLineItemViewHolder(view)
                R.layout.list_item_header -> HeaderViewHolder(view)
                else -> throw RuntimeException("unknown view type: $viewType")
            }
        }

    }

}