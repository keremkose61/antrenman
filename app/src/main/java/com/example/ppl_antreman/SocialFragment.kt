package com.pplantreman.app

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment

class SocialFragment : Fragment(R.layout.fragment_social) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find all buttons in the layout that need click listeners
        // The first button is the clap for friend 1
        // The second button is the fire for friend 2
        // The third button is the Leaderboard
        val buttons = mutableListOf<Button>()
        findButtons(view, buttons)

        // Simplistic click handling for mock UI
        for (btn in buttons) {
            btn.setOnClickListener {
                val ctx = context ?: return@setOnClickListener
                when (btn.text.toString()) {
                    "👏" -> Toast.makeText(ctx, "Tebrik gönderildi!", Toast.LENGTH_SHORT).show()
                    "🔥" -> Toast.makeText(ctx, "Ateş gönderildi!", Toast.LENGTH_SHORT).show()
                    "🏆 Haftalık Liderlik Tablosu" -> Toast.makeText(ctx, "Liderlik tablosu hazırlanıyor...", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun findButtons(view: View, buttons: MutableList<Button>) {
        if (view is android.view.ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                if (child is Button) {
                    buttons.add(child)
                } else if (child is android.view.ViewGroup) {
                    findButtons(child, buttons)
                }
            }
        } else if (view is Button) {
            buttons.add(view)
        }
    }
}
