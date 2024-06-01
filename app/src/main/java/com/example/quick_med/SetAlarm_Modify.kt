package com.example.quick_med

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.quick_med.databinding.FragmentSetAlarmModifyBinding

class SetAlarmModify : DialogFragment() {
    private var _binding: FragmentSetAlarmModifyBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSetAlarmModifyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val alarmName = arguments?.getString("ALARM_NAME")
        val alarmHour = arguments?.getInt("ALARM_HOUR") ?: -1
        val alarmMinute = arguments?.getInt("ALARM_MINUTE") ?: -1

        binding.alarmNameEditText.setText(alarmName) // 알람 이름 설정
        binding.alarmTimePicker.setIs24HourView(true) // 24시간 형식 설정

        if (Build.VERSION.SDK_INT >= 23) {
            binding.alarmTimePicker.hour = alarmHour
            binding.alarmTimePicker.minute = alarmMinute
        } else {
            binding.alarmTimePicker.currentHour = alarmHour
            binding.alarmTimePicker.currentMinute = alarmMinute
        }

        binding.saveButton.setOnClickListener {
            // 저장 로직 구현
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
