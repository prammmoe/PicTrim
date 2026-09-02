package com.prammmoe.pictrim

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import com.prammmoe.pictrim.domain.model.EditorMode
import com.prammmoe.pictrim.ui.editor.EditorScreen
import com.prammmoe.pictrim.ui.editor.EditorViewModel
import com.prammmoe.pictrim.ui.batch.BatchEditorScreen
import com.prammmoe.pictrim.ui.batch.BatchEditorViewModel
import com.prammmoe.pictrim.ui.batch.BatchResultScreen
import com.prammmoe.pictrim.ui.batch.BatchResultViewModel
import com.prammmoe.pictrim.ui.home.HomeScreen
import com.prammmoe.pictrim.ui.preview.PreviewScreen
import com.prammmoe.pictrim.ui.preview.PreviewViewModel
import com.prammmoe.pictrim.ui.result.ResultScreen
import com.prammmoe.pictrim.ui.result.ResultViewModel
import com.prammmoe.pictrim.ui.theme.PicTrimTheme
import com.prammmoe.pictrim.ui.onboarding.OnboardingGateViewModel
import com.prammmoe.pictrim.ui.onboarding.OnboardingScreen
import com.prammmoe.pictrim.ui.onboarding.OnboardingViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { PicTrimTheme { PicTrimApp() } }
    }
}

private object Routes {
    const val HOME = "home"
    const val ONBOARDING = "onboarding"
    const val PREVIEW = "preview/{uri}"
    const val EDITOR = "editor/{uri}/{mode}"
    const val RESULT = "result/{originalUri}/{resultUri}"
    const val BATCH_EDITOR = "batchEditor"
    const val BATCH_RESULT = "batchResult/{jobId}"
    fun preview(uri: Uri) = "preview/${Uri.encode(uri.toString())}"
    fun editor(uri: Uri, mode: EditorMode) = "editor/${Uri.encode(uri.toString())}/${mode.name}"
    fun result(original: Uri, output: Uri) = "result/${Uri.encode(original.toString())}/${Uri.encode(output.toString())}"
    fun batchResult(jobId: String) = "batchResult/$jobId"
}

@Composable
fun PicTrimApp() {
    val nav = rememberNavController()
    val gate: OnboardingGateViewModel = hiltViewModel()
    val onboardingComplete = gate.completed.collectAsState(initial = null).value
    val context = LocalContext.current
    val picker = androidx.activity.compose.rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> uri?.let { nav.navigate(Routes.preview(it)) } }
    if (onboardingComplete == null) return
    Scaffold(Modifier.fillMaxSize()) { padding ->
        NavHost(navController = nav, startDestination = if (onboardingComplete == true) Routes.HOME else Routes.ONBOARDING, modifier = Modifier.padding(padding)) {
            composable(Routes.ONBOARDING) {
                val vm: OnboardingViewModel = hiltViewModel()
                val page = vm.page.collectAsStateValue()
                OnboardingScreen(
                    page = page,
                    onNext = vm::next,
                    onBack = vm::back,
                    onSkip = { vm.finish { nav.navigate(Routes.HOME) { popUpTo(Routes.ONBOARDING) { inclusive = true } } } },
                    onFinish = { vm.finish { nav.navigate(Routes.HOME) { popUpTo(Routes.ONBOARDING) { inclusive = true } } } }
                )
            }
            composable(Routes.HOME) { HomeScreen(onChooseImage = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, onChooseImages = { nav.navigate(Routes.BATCH_EDITOR) }) }
            composable(Routes.BATCH_EDITOR) {
                val vm: BatchEditorViewModel = hiltViewModel(); val state = vm.state.collectAsStateValue()
                val multiPicker = androidx.activity.compose.rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(50)) { uris -> vm.select(uris) }
                LaunchedEffect(state.jobId) { state.jobId?.let { nav.navigate(Routes.batchResult(it)) } }
                BatchEditorScreen(state, onBack = nav::popBackStack, onSelect = { multiPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, onStart = vm::start, onQuality = vm::quality, onTarget = vm::target, onWidth = vm::width, onHeight = vm::height, onFormat = vm::format, onCrop = vm::crop, onRemoveMetadata = vm::removeMetadata)
            }
            composable(Routes.PREVIEW, arguments = listOf(navArgument("uri") { type = NavType.StringType })) {
                val vm: PreviewViewModel = hiltViewModel()
                PreviewScreen(vm.state.collectAsStateValue(), onBack = nav::popBackStack, onMode = { mode -> nav.navigate(Routes.editor(Uri.parse(Uri.decode(it.arguments?.getString("uri")!!)), mode)) }, onRetry = vm::load)
            }
            composable(Routes.EDITOR, arguments = listOf(navArgument("uri") { type = NavType.StringType }, navArgument("mode") { type = NavType.StringType })) {
                val vm: EditorViewModel = hiltViewModel(); val state = vm.state.collectAsStateValue()
                LaunchedEffect(state.resultUri) { state.resultUri?.let { output -> nav.navigate(Routes.result(state.originalUri, output)) } }
                EditorScreen(state, nav::popBackStack, vm::process, vm::setQuality, vm::setTarget, vm::setFormat, vm::setWidth, vm::setHeight, vm::setPercentage, vm::setKeepAspect, vm::setCrop, vm::setRemoveMetadata)
            }
            composable(Routes.RESULT, arguments = listOf(navArgument("originalUri") { type = NavType.StringType }, navArgument("resultUri") { type = NavType.StringType })) {
                val vm: ResultViewModel = hiltViewModel()
                ResultScreen(
                    state = vm.state.collectAsStateValue(),
                    onBack = nav::popBackStack,
                    onSave = vm::save,
                    onShowInGallery = { vm.showInGallery()?.let { intent -> runCatching { context.startActivity(intent) } } },
                    onShare = { vm.share()?.let { intent -> runCatching { context.startActivity(intent) } } },
                    onAnother = { nav.navigate(Routes.HOME) { popUpTo(Routes.HOME) { inclusive = false } } }
                )
            }
            composable(Routes.BATCH_RESULT, arguments = listOf(navArgument("jobId") { type = NavType.StringType })) {
                val vm: BatchResultViewModel = hiltViewModel()
                BatchResultScreen(vm.state.collectAsStateValue(), nav::popBackStack, vm::saveAll) { savedUri -> runCatching { context.startActivity(vm.openGallery(savedUri)) } }
            }
        }
    }
}

@Composable
private fun <T> kotlinx.coroutines.flow.StateFlow<T>.collectAsStateValue(): T = collectAsState().value
