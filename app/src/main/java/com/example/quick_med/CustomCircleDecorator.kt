package com.example.quick_med

import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class CustomCircleCalendarDecorator(private val date: CalendarDay, private val color: Int) : DayViewDecorator {

    private val drawable: Drawable

    init {
        val shape = ShapeDrawable(OvalShape())
        shape.paint.color = color
        drawable = shape
    }

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return day == date
    }

    override fun decorate(view: DayViewFacade) {
        view.setBackgroundDrawable(drawable)
    }
}
