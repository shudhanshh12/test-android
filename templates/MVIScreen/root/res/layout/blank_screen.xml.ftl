<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/grey50"
    tools:context="${escapeKotlinIdentifiers(packageName)}.${featureName?lower_case}.${featureName}Screen">

    <com.google.android.material.appbar.AppBarLayout
        android:id="@+id/appbar"
        android:layout_width="0dp"
        android:layout_height="56dp"
        android:theme="@style/LightActionBarTheme"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent">

        <androidx.appcompat.widget.Toolbar
            android:id="@+id/toolbar"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            app:contentInsetStart="0dp"
            app:contentInsetStartWithNavigation="0dp"
            app:navigationIcon="?attr/homeAsUpIndicator">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_gravity="center_vertical"
                android:maxLines="1"
                android:paddingStart="8dp"
                android:text="Hello world"
                android:textAppearance="@style/OKC.TextAppearance.Headline6"
                android:textColor="@color/grey900"
                android:textStyle="bold"
                tools:ignore="RtlSymmetry" />

        </androidx.appcompat.widget.Toolbar>

    </com.google.android.material.appbar.AppBarLayout>

    <#if includeEpoxy>
    <com.airbnb.epoxy.EpoxyRecyclerView
        android:id="@+id/recycler_view"
        android:layout_width="0dp"
        android:layout_height="0dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/appbar" />
    </#if>

</androidx.constraintlayout.widget.ConstraintLayout>
