package com.camera._di

import com.camera.CameraActivity
import com.camera.CapturedImageActivity
import com.camera.selected_image.MultipleImageActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class CameraModule {

    @ContributesAndroidInjector
    abstract fun multipleImageActivity(): MultipleImageActivity

    @ContributesAndroidInjector
    abstract fun capturedImageActivity(): CapturedImageActivity

    @ContributesAndroidInjector
    abstract fun cameraActivity(): CameraActivity
}
