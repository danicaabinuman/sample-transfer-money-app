package com.unionbankph.corporate.loan.calculator

import android.content.Context
import android.graphics.Color
import android.view.View
import com.airbnb.epoxy.*
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.unionbankph.corporate.R
import com.unionbankph.corporate.databinding.*
import java.text.DecimalFormat

@EpoxyModelClass(layout = R.layout.item_monthly_payment_breakdown)
abstract class MonthlyPaymentBreakdownMainModel :
    EpoxyModelWithHolder<MonthlyPaymentBreakdownMainModel.Holder>() {

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    var principal: Float? = 0f

    @EpoxyAttribute
    var interest: Float? = 0f

    @EpoxyAttribute
    var monthlyPayment: Float? = null

    override fun bind(holder: Holder) {
        holder.binding.apply {

            initViews(monthlyPaymentBreakdownPcChart)
            monthlyPaymentBreakdownPcChart.apply {

                if (principal != 0f && interest != 0f) {

                    val monthly = principal?.plus(interest ?: 0f)
                    monthlyPaymentBreakdownTvMonthlyPaymentValue.text = context.getString(R.string.format_php, monthly)
                    monthlyPaymentBreakdownTvInterestValue.text = context.getString(R.string.format_php, interest)
                    monthlyPaymentBreakdownTvPrincipalValue.text = context.getString(R.string.format_php, principal)

                    val dataEntries = ArrayList<PieEntry>()
                    dataEntries.add(PieEntry(principal?.toFloat() ?: 0f, "Principal"))
                    dataEntries.add(PieEntry(interest?.toFloat() ?: 0f, "Interest"))

                    val dataColor: ArrayList<Int> = ArrayList()
                    dataColor.add(Color.parseColor("#4DD0E1"))
                    dataColor.add(Color.parseColor("#FF8A65"))

                    val dataSet = PieDataSet(dataEntries, "")
                    dataSet.apply {
                        sliceSpace = 0f
                        colors = dataColor
                        xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
                        yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
                        valueLinePart1OffsetPercentage = 9f
                        valueLinePart1Length = 0.9f
                        valueLinePart2Length = 0.7f
                        valueLineColor = Color.TRANSPARENT
                        setValueTextColors(dataColor)
                    }

                    var result = PieData(dataSet)
                    val format = PercentFormatter(this).apply {
                        //mFormat = DecimalFormat("###,###,##")
                    }
                    result.setValueFormatter(format)
                    data = result
                    data.setValueTextSize(12f)
                    setExtraOffsets(0f, 15f, 0f, 15f)
                    //animateY(1400, Easing.EaseInOutQuad)
                    setDrawCenterText(false)
                    invalidate()
                }
            }
        }
    }

    fun initViews(monthlyPaymentBreakdownPcChart: PieChart) {
        monthlyPaymentBreakdownPcChart.apply {
            description.text = ""
            setUsePercentValues(true)
            isDrawHoleEnabled = false
            setTouchEnabled(false)
            setDrawEntryLabels(false)
            isRotationEnabled = false
            legend.isEnabled = false

        }
    }

    class Holder : EpoxyHolder() {
        lateinit var binding: ItemMonthlyPaymentBreakdownBinding
        override fun bindView(itemView: View) {
            binding = ItemMonthlyPaymentBreakdownBinding.bind(itemView)
        }
    }
}