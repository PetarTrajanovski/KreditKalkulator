package mk.capitalbank.kreditkalkulator

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import mk.capitalbank.kreditkalkulator.databinding.ActivityMainBinding
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow

private const val MIN_TERM_LENGTH = 6
private const val MAX_TERM_LENGTH = 36
private const val MIN_AMOUNT = 1000
private const val MAX_AMOUNT = 180000

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val numberFormat = DecimalFormat("###,###")

    private var termLength: Int = 6
        set(value) {
            field = value
            calculate()
        }
    private var amount: Int = 1000
        set(value) {
            field = value
            calculate()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.setOnClickListener {
            it.hideKeyboard()
            binding.amountEditText.clearFocus()
            binding.termLengthEditText.clearFocus()
        }

        // Amount
        binding.amountSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val amount = (progress + 100) * 10
                    binding.amountEditText.setText(amount.toString())
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })
        binding.amountEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrBlank()) {
                    amount = s.toString().toInt()
                    binding.amountSeekBar.progress = (amount / 10) - 100
                }
            }
        })

        // Term Length
        binding.termLengthSeekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val amount = progress + 6
                    binding.termLengthEditText.setText(amount.toString())
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
        binding.termLengthEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrBlank()) {
                    termLength = s.toString().toInt()
                    binding.termLengthSeekBar.progress = termLength - 6
                }
            }
        })

        binding.termLengthEditText.setText("12")
        binding.amountEditText.setText("10000")
    }

    private fun calculate() {
        if (validateInput()) {
            val r = 0.000000001 / 1200
            val r1 = (r + 1).pow(termLength)

            val provisionPercent = provisionForTermLength(termLength)

            val rateWithoutProvision = ((r + r / (r1 - 1)) * amount)
            val totalProvision: Double = amount.toDouble() * provisionPercent / 100 / termLength
            val rate = rateWithoutProvision + totalProvision
            val total = rate * termLength

            binding.rateLabel.text = getString(R.string.rate_label, termLength)
            binding.rate.text = numberFormat.format(rate).replace(",", " ")
            binding.provision.text = "$provisionPercent%"
            binding.total.text = numberFormat.format(total).replace(",", " ") + " ден."

            Calendar.getInstance().apply {
                add(Calendar.MONTH, 1)
                binding.firstPayment.text =
                    SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(time)
                add(Calendar.MONTH, termLength)
                binding.lastPayment.text =
                    SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(time)
            }
        } else {
            binding.rateLabel.text = getString(R.string.rate_label_empty)
            binding.rate.text = "--"
            binding.provision.text = "--"
            binding.total.text = "--"
        }
    }

    private fun validateInput(): Boolean {
        var inputValid = true
        if (termLength < MIN_TERM_LENGTH || termLength > MAX_TERM_LENGTH) {
            inputValid = false
            binding.termLengthEditText.setTextColor(ContextCompat.getColor(this, R.color.text_error))
        } else {
            binding.termLengthEditText.setTextColor(ContextCompat.getColor(this, R.color.black87))
        }

        if (amount < MIN_AMOUNT || amount > MAX_AMOUNT) {
            inputValid = false
            binding.amountEditText.setTextColor(ContextCompat.getColor(this, R.color.text_error))
        } else {
            binding.amountEditText.setTextColor(ContextCompat.getColor(this, R.color.black87))
        }
        return inputValid
    }

    private fun provisionForTermLength(termLength: Int): Int {
        return when {
            termLength < 13 -> 10
            termLength < 19 -> 12
            termLength < 25 -> 16
            else -> 25
        }
    }
}

fun View.hideKeyboard() = this.let { view ->
    val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    imm?.apply { hideSoftInputFromWindow(view.windowToken, 0) }
}

