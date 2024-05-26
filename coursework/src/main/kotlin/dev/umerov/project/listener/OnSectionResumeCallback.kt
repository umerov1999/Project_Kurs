package dev.umerov.project.listener

import dev.umerov.project.model.SectionItem

interface OnSectionResumeCallback {
    fun onSectionResume(@SectionItem section: Int)
}