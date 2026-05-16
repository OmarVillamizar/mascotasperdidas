package com.mascotasperdidas.app.app.ui.screens.permissions

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel para la pantalla de permisos.
 *
 * Sin puertos in — los permisos son tema puramente Android.
 * En Fase 9+ se integrará con PermissionUtils para chequear/otorgar permisos.
 */
@HiltViewModel
class PermissionsViewModel @Inject constructor() : ViewModel()
