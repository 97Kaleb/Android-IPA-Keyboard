package com.example.ipakeyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.helper.widget.Flow
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection

enum class VoicingState {
    UNVOICED,
    VOICED,
    LOCKED_VOICED
}
enum class RoundingState {
    UNROUNDED,
    ROUNDED
}
enum class Layout {
    PULMONIC,
    VOWEL,
    NONPULMONIC,
    DIACRITIC,
    SYMBOL
}

class IPAInputMethodService : InputMethodService() {
    private val unvoicedNasals = listOf("m̥", "ɱ̊", "n̪̊", "n̥", "n̠̊", "ɳ̊", "ɲ̊", "ŋ̊", "ɴ̥")
    private val voicedNasals = listOf("m", "ɱ", "n̼", "n̪", "n", "n̠", "ɳ", "ɲ", "ŋ", "ɴ")
    private val unvoicedStops = listOf("p", "p̪", "t̼", "t̪", "t", "ʈ", "c", "k", "q", "ʡ", "ʔ")
    private val voicedStops = listOf("b", "b̪", "d̼", "d̪", "d", "ɖ", "ɟ", "g", "ɢ")
    private val unvoicedSibFrics = listOf("s̪", "s", "ʃ", "ʂ", "ɕ")
    private val voicedSibFrics = listOf("z̪", "z", "ʒ", "ʐ", "ʑ")
    private val unvoicedNSibFrics = listOf("ɸ", "f", "θ̼", "θ", "θ̠", "ɹ̠̊˔", "ɻ̊˔", "ç", "x", "χ", "ħ", "h")
    private val voicedNSibFrics = listOf("β", "v", "ð̼", "ð", "ð̠", "ɹ̠˔", "ɻ˔", "ʝ", "ɣ", "ʁ", "ʕ", "ɦ")
    private val approximants = listOf("β̞", "ʋ", "ð̞", "ɹ", "ɹ̠", "ɻ", "j", "ɰ", "ʁ̞", "ʔ̞")
    private val tapsNflaps = listOf("ⱱ̟", "ⱱ", "ɾ̼", "ɾ", "ɽ", "ɢ̆", "ʡ̮")
    private val unvoicedTrills = listOf("ʙ̥", "r̥", "ɽ̊r̥", "ʀ̥", "ʜ")
    private val voicedTrills = listOf("ʙ", "r", "ɽr", "ʀ", "ʢ")
    private val unvoicedLatFric = listOf("ɬ̪", "ɬ", "ꞎ", "𝼆", "𝼄")
    private val voicedLatFric = listOf("ɮ", "𝼅", "ʎ̝", "ʟ̝")
    private val unvoicedLatApp = listOf("l̥", "ɭ̊", "ʎ̥", "ʟ̥")
    private val voicedLatApp = listOf("l̪", "l", "l̠", "ɭ", "ʎ", "ʟ", "ʟ̠")
    private val latTap = listOf("ɺ", "𝼈", "ʎ̮", "ʟ̆")

    private val unroundedVowels = listOf("i", "ɨ", "ɯ", "ɪ", "e", "ɘ", "ɤ", "e̞", "ə", "ɤ̞", "ɛ", "ɜ", "ʌ", "æ", "ɐ", "a", "ä", "ɑ")
    private val roundedVowels = listOf("y", "ʉ", "u", "ʏ", "ʊ", "ø", "ɵ", "o", "ø̞", "ə", "o̞", "œ", "ɞ", "ɔ", "ɐ", "ɶ", "ɒ")

    private val unvoicedImplosives = listOf("ɓ", "ɗ", "ʄ", "ɠ", "ʛ")
    private val voicedImplosives = listOf("ɓ̥ ", "ɗ̥ ", "ᶑ̊", "ʄ̊", "ɠ̊", "ʛ̥")
    private val nonPulmonicSymbols = listOf("ʼ", "ʘ", "ǀ", "ǃ", "𝼊", "ǂ", "ǁ")

    private val diacritics1 = listOf("◌̀", "◌́", "◌̂", "◌̃", "◌̄", "◌̅", "◌̆", "◌̇", "◌̈", "◌̉", "◌̊", "◌̋", "◌̌", "◌̍", "◌̎", "◌̏")
    private val diacritics2 = listOf("◌̐", "◌̑", "◌̒", "◌̓", "◌̔", "◌̕", "◌̖", "◌̗", "◌̘", "◌̙", "◌̚", "◌̛", "◌̜", "◌̝", "◌̞", "◌̟")
    private val diacritics3 = listOf("◌̠", "◌̡", "◌̢", "◌̣", "◌̤", "◌̥", "◌̦", "◌̧", "◌̨", "◌̩", "◌̪", "◌̫", "◌̬", "◌̭", "◌̮", "◌̯")
    private val diacritics4 = listOf("◌̰", "◌̱", "◌̲", "◌̳", "◌̴", "◌̵", "◌̶", "◌̷", "◌̸", "◌̹", "◌̺", "◌̻", "◌̼", "◌̽", "◌̾", "◌̿")
    private val diacritics5 = listOf("◌̀", "◌́", "◌͂", "◌̓", "◌̈́", "◌ͅ", "◌͆", "◌͇", "◌͈", "◌͉", "◌͊", "◌͋", "◌͌", "◌͍", "◌͎", "CGJ")
    private val diacritics6 = listOf("◌͐", "◌͑", "◌͒", "◌͓", "◌͔", "◌͕", "◌͖", "◌͗", "◌͘", "◌͙", "◌͚", "◌͛", "◌͜◌", "◌͝◌", "◌͞◌", "◌͟◌")
    private val diacritics7 = listOf("◌͠◌", "◌͡◌", "◌͢◌", "◌ͣ", "◌ͤ", "◌ͥ", "◌ͦ", "◌ͧ", "◌ͨ", "◌ͩ", "◌ͪ", "◌ͫ", "◌ͬ", "◌ͭ", "◌ͮ", "◌ͯ")
    private val symbols = listOf("[", "]", "/", "{", "}", "(", ")", "⸨", "⸩", "ː", "꜒", "꜓", "꜔", "꜕", "꜖")
    private var voicingState = VoicingState.UNVOICED
    private var roundingState = RoundingState.UNROUNDED
    private var currentLayout: Layout = Layout.PULMONIC
    private var currentManner: String = "nasal"
    private var currentNonPulmonicManner: String = "imp"
    private var currentDiacritics: String = "d1"


    override fun onCreateInputView(): View {
        val view = layoutInflater.inflate(R.layout.keyboard_view, null)
        // Sub-layout switch keys
        val pulmonicMannerButtons = listOf<Button>(
            view.findViewById(R.id.btnNasal),
            view.findViewById(R.id.btnStop),
            view.findViewById(R.id.btnNSibFric),
            view.findViewById(R.id.btnSibFric),
            view.findViewById(R.id.btnApprox),
            view.findViewById(R.id.btnTap),
            view.findViewById(R.id.btnTrill),
            view.findViewById(R.id.btnLatFric),
            view.findViewById(R.id.btnLatApp),
            view.findViewById(R.id.btnLatTap)
        )
        val nonPulmonicMannerButtons = listOf<Button>(
            view.findViewById(R.id.btnImplosive),
            view.findViewById(R.id.btnNonPulmonicSymbols)
        )
        val diacriticButtons = listOf<Button>(
            view.findViewById(R.id.btnD1),
            view.findViewById(R.id.btnD2),
            view.findViewById(R.id.btnD3),
            view.findViewById(R.id.btnD4),
            view.findViewById(R.id.btnD5),
            view.findViewById(R.id.btnD6),
            view.findViewById(R.id.btnD7)
        )
        val voicingButton = view.findViewById<Button>(R.id.btnVoicing)
        val roundingButton = view.findViewById<Button>(R.id.btnRounding)
        // Layout switch keys
        val pulmonicButton = view.findViewById<Button>(R.id.btnPulmonic)
        val vowelsButton = view.findViewById<Button>(R.id.btnVowels)
        val nonPulmonicButton = view.findViewById<Button>(R.id.btnNonPulmonic)
        val diacriticButton = view.findViewById<Button>(R.id.btnDiacritics)
        val symbolButton = view.findViewById<Button>(R.id.btnSymbols)
        val container = view.findViewById<ConstraintLayout>(R.id.rootLayout)
        val keyFlow = view.findViewById<Flow>(R.id.keyFlow)

        // Set to Default Layout
        updateLayoutMode(
            currentLayout,
            voicingButton,
            roundingButton,
            pulmonicButton,
            vowelsButton,
            nonPulmonicButton,
            diacriticButton,
            symbolButton,
            pulmonicMannerButtons,
            nonPulmonicMannerButtons,
            diacriticButtons,
            container,
            keyFlow
        )
        populateKeys(container, keyFlow, unvoicedNasals)

        // Sub-layout switch functionality
        view.findViewById<Button>(R.id.btnNasal)?.setOnClickListener {
            currentManner = "nasal"
            populateKeys(container, keyFlow, getCurrentManner())
        }
        view.findViewById<Button>(R.id.btnStop)?.setOnClickListener {
            currentManner = "stop"
            populateKeys(container, keyFlow, getCurrentManner())
        }
        view.findViewById<Button>(R.id.btnNSibFric)?.setOnClickListener {
            currentManner = "nsibfric"
            populateKeys(container, keyFlow, getCurrentManner())
        }
        view.findViewById<Button>(R.id.btnSibFric)?.setOnClickListener {
            currentManner = "sibfric"
            populateKeys(container, keyFlow, getCurrentManner())
        }
        view.findViewById<Button>(R.id.btnApprox)?.setOnClickListener {
            currentManner = "approx"
            populateKeys(container, keyFlow, getCurrentManner())
        }
        view.findViewById<Button>(R.id.btnTap)?.setOnClickListener {
            currentManner = "tap"
            populateKeys(container, keyFlow, getCurrentManner())
        }
        view.findViewById<Button>(R.id.btnTrill)?.setOnClickListener {
            currentManner = "trill"
            populateKeys(container, keyFlow, getCurrentManner())
        }
        view.findViewById<Button>(R.id.btnLatFric)?.setOnClickListener {
            currentManner = "latfric"
            populateKeys(container, keyFlow, getCurrentManner())
        }
        view.findViewById<Button>(R.id.btnLatApp)?.setOnClickListener {
            currentManner = "latapp"
            populateKeys(container, keyFlow, getCurrentManner())
        }
        view.findViewById<Button>(R.id.btnLatTap)?.setOnClickListener {
            currentManner = "lattap"
            populateKeys(container, keyFlow, getCurrentManner())
        }
        view.findViewById<Button>(R.id.btnImplosive)?.setOnClickListener {
            currentNonPulmonicManner = "imp"
            populateKeys(container, keyFlow, getCurrentNonPulmonicManner())
        }
        view.findViewById<Button>(R.id.btnNonPulmonicSymbols)?.setOnClickListener {
            currentNonPulmonicManner = "sym"
            populateKeys(container, keyFlow, getCurrentNonPulmonicManner())
        }
        view.findViewById<Button>(R.id.btnD1)?.setOnClickListener {
            currentDiacritics = "d1"
            populateKeys(container, keyFlow, getCurrentDiacritics())
        }
        view.findViewById<Button>(R.id.btnD2)?.setOnClickListener {
            currentDiacritics = "d2"
            populateKeys(container, keyFlow, getCurrentDiacritics())
        }
        view.findViewById<Button>(R.id.btnD3)?.setOnClickListener {
            currentDiacritics = "d3"
            populateKeys(container, keyFlow, getCurrentDiacritics())
        }
        view.findViewById<Button>(R.id.btnD4)?.setOnClickListener {
            currentDiacritics = "d4"
            populateKeys(container, keyFlow, getCurrentDiacritics())
        }
        view.findViewById<Button>(R.id.btnD5)?.setOnClickListener {
            currentDiacritics = "d5"
            populateKeys(container, keyFlow, getCurrentDiacritics())
        }
        view.findViewById<Button>(R.id.btnD6)?.setOnClickListener {
            currentDiacritics = "d6"
            populateKeys(container, keyFlow, getCurrentDiacritics())
        }
        view.findViewById<Button>(R.id.btnD7)?.setOnClickListener {
            currentDiacritics = "d7"
            populateKeys(container, keyFlow, getCurrentDiacritics())
        }
        voicingButton?.setOnClickListener {
            voicingState = when (voicingState) {
                VoicingState.UNVOICED -> VoicingState.VOICED
                VoicingState.VOICED -> VoicingState.LOCKED_VOICED
                VoicingState.LOCKED_VOICED -> VoicingState.UNVOICED
            }

            voicingButton.text = when (voicingState) {
                VoicingState.UNVOICED -> "Ø"
                VoicingState.VOICED -> "O"
                VoicingState.LOCKED_VOICED -> "O᪲"
            }

            populateKeys(container, keyFlow, getCurrentManner())
        }
        roundingButton?.setOnClickListener {
            roundingState = when (roundingState) {
                RoundingState.UNROUNDED -> RoundingState.ROUNDED
                RoundingState.ROUNDED -> RoundingState.UNROUNDED
            }

            roundingButton.text = when (roundingState) {
                RoundingState.UNROUNDED -> "◌͑"
                RoundingState.ROUNDED -> "◌͗"
            }

            if (currentLayout == Layout.VOWEL) {
                populateKeys(container, keyFlow, getCurrentVowelSet())
            }
        }

        // Enter, space, and backspace functionality
        view.findViewById<Button>(R.id.btnSpace)?.setOnClickListener {
            currentInputConnection?.commitText(" ", 1)
        }

        view.findViewById<Button>(R.id.btnBackspace)?.setOnClickListener {
            val ic = currentInputConnection
            val selectedText = ic?.getSelectedText(InputConnection.GET_TEXT_WITH_STYLES)
            if (!selectedText.isNullOrEmpty()) {
                ic?.commitText("", 1)
            } else {
                ic?.deleteSurroundingText(1, 0)
            }
        }

        view.findViewById<Button>(R.id.btnEnter)?.setOnClickListener {
            val editorInfo = currentInputEditorInfo
            val actionId = editorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION)

            if (actionId != null && actionId != EditorInfo.IME_ACTION_NONE) {
                currentInputConnection?.performEditorAction(actionId)
            } else {
                currentInputConnection?.commitText("\n", 1)
            }
        }

        // Layout Switch Keys
        view.findViewById<Button>(R.id.btnVowels)?.setOnClickListener {
            currentLayout = Layout.VOWEL
            updateLayoutMode(
                currentLayout,
                voicingButton,
                roundingButton,
                pulmonicButton,
                vowelsButton,
                nonPulmonicButton,
                diacriticButton,
                symbolButton,
                pulmonicMannerButtons,
                nonPulmonicMannerButtons,
                diacriticButtons,
                container,
                keyFlow
            )
            populateKeys(container, keyFlow, getCurrentVowelSet())
        }

        view.findViewById<Button>(R.id.btnPulmonic)?.setOnClickListener {
            currentLayout = Layout.PULMONIC
            updateLayoutMode(
                currentLayout,
                voicingButton,
                roundingButton,
                pulmonicButton,
                vowelsButton,
                nonPulmonicButton,
                diacriticButton,
                symbolButton,
                pulmonicMannerButtons,
                nonPulmonicMannerButtons,
                diacriticButtons,
                container,
                keyFlow
            )
            populateKeys(container, keyFlow, getCurrentManner())
        }
        view.findViewById<Button>(R.id.btnNonPulmonic)?.setOnClickListener {
            currentLayout = Layout.NONPULMONIC
            updateLayoutMode(
                currentLayout,
                voicingButton,
                roundingButton,
                pulmonicButton,
                vowelsButton,
                nonPulmonicButton,
                diacriticButton,
                symbolButton,
                pulmonicMannerButtons,
                nonPulmonicMannerButtons,
                diacriticButtons,
                container,
                keyFlow
            )
            populateKeys(container, keyFlow, getCurrentNonPulmonicManner())
        }

        view.findViewById<Button>(R.id.btnDiacritics)?.setOnClickListener {
            currentLayout = Layout.DIACRITIC
            updateLayoutMode(
                currentLayout,
                voicingButton,
                roundingButton,
                pulmonicButton,
                vowelsButton,
                nonPulmonicButton,
                diacriticButton,
                symbolButton,
                pulmonicMannerButtons,
                nonPulmonicMannerButtons,
                diacriticButtons,
                container,
                keyFlow
            )
            populateKeys(container, keyFlow, getCurrentDiacritics())
        }

        view.findViewById<Button>(R.id.btnSymbols)?.setOnClickListener {
            currentLayout = Layout.SYMBOL
            updateLayoutMode(
                currentLayout,
                voicingButton,
                roundingButton,
                pulmonicButton,
                vowelsButton,
                nonPulmonicButton,
                diacriticButton,
                symbolButton,
                pulmonicMannerButtons,
                nonPulmonicMannerButtons,
                diacriticButtons,
                container,
                keyFlow
            )
            populateKeys(container, keyFlow, symbols)
        }

        return view
    }

    private var currentKeyIds: List<Int> = emptyList()


    // Displays the keys
    private fun populateKeys(container: ConstraintLayout, flow: Flow, symbols: List<String>) {
        currentKeyIds.forEach { id ->
            container.findViewById<View>(id)?.let { container.removeView(it) }
        }

        val newIds = mutableListOf<Int>()
        val voicingButton = (window?.window?.decorView?.findViewById<Button>(R.id.btnVoicing))

        symbols.forEach { symbol ->
            val button = Button(this).apply {
                text = symbol
                id = View.generateViewId()
                setOnClickListener {
                    val output = getOutputText(symbol)
                    currentInputConnection?.commitText(output, 1)
                    resetVoicing(voicingButton, container, flow)
                }
            }
            container.addView(button)
            newIds.add(button.id)
        }

        flow.referencedIds = newIds.toIntArray()
        currentKeyIds = newIds
    }

    // Determines which keys should be present on this sub-layout for the Pulmonic consonants
    private fun getCurrentManner(): List<String> {
        val isVoiced = voicingState != VoicingState.UNVOICED

        return when (currentManner) {
            "nasal" -> if (isVoiced) voicedNasals else unvoicedNasals
            "stop" -> if (isVoiced) voicedStops else unvoicedStops
            "sibfric" -> if (isVoiced) voicedSibFrics else unvoicedSibFrics
            "nsibfric" -> if (isVoiced) voicedNSibFrics else unvoicedNSibFrics
            "trill" -> if (isVoiced) voicedTrills else unvoicedTrills
            "latfric" -> if (isVoiced) voicedLatFric else unvoicedLatFric
            "latapp" -> if (isVoiced) voicedLatApp else unvoicedLatApp
            "approx" -> approximants
            "tap" -> tapsNflaps
            "lattap" -> latTap
            else -> emptyList()
        }
    }

    // Determines which keys should be present on this sub-layout for the Non-Pulmonic consonants
    private fun getCurrentNonPulmonicManner(): List<String> {
        val isVoiced = voicingState != VoicingState.UNVOICED

        return when (currentNonPulmonicManner) {
            "imp" -> if (isVoiced) voicedImplosives else unvoicedImplosives
            "sym" ->  nonPulmonicSymbols
            else -> emptyList()
        }
    }

    // Determines which keys should be present on this sub-layout for the diacritics
    private fun getCurrentDiacritics(): List<String> {
        return when (currentDiacritics) {
            "d1" -> diacritics1
            "d2" -> diacritics2
            "d3" -> diacritics3
            "d4" -> diacritics4
            "d5" -> diacritics5
            "d6" -> diacritics6
            "d7" -> diacritics7
            else -> emptyList()
        }
    }

    // Determines which keys should be present on this sub-layout for the vowels
    private fun getCurrentVowelSet(): List<String> {
        return when (roundingState) {
            RoundingState.UNROUNDED -> unroundedVowels
            RoundingState.ROUNDED -> roundedVowels
        }
    }

    // Resets voicing when navigating away from a layout that has a voicing key
    private fun resetVoicing(voicingButton: Button?, container: ConstraintLayout, flow: Flow) {
        if (voicingState == VoicingState.VOICED || (currentLayout != Layout.PULMONIC && currentLayout != Layout.NONPULMONIC && voicingState == VoicingState.LOCKED_VOICED)) {
            voicingState = VoicingState.UNVOICED
            voicingButton?.text = "Ø"
            populateKeys(container, flow, getCurrentManner())
        }
    }

    // Resets rounding when navigating away from a layout that has a rounding key (which is only the
    // vowels layout)
    private fun resetRounding(roundingButton: Button?, container: ConstraintLayout, flow: Flow) {
        if (roundingState == RoundingState.ROUNDED) {
            roundingState = RoundingState.UNROUNDED
            roundingButton?.text = "◌͑"
            populateKeys(container, flow, getCurrentManner())
        }
    }

    // Updates the view to ensure a specific layout's keys are visible, and only that layouts keys
    private fun updateLayoutMode(
        layout: Layout,
        voicingButton: Button?,
        roundingButton: Button?,
        pulmonicButton: Button?,
        vowelsButton: Button?,
        nonPulmonicButton: Button?,
        diacriticButton: Button?,
        symbolButton: Button?,
        pulmonicMannerButtons: List<Button>,
        nonPulmonicMannerButtons: List<Button> = emptyList(),
        diacriticButtons: List<Button> = emptyList(),
        container: ConstraintLayout,
        flow: Flow
    ) {
        when (layout) {
            Layout.PULMONIC -> {
                voicingButton?.visibility = View.VISIBLE
                roundingButton?.visibility = View.GONE
                pulmonicButton?.visibility = View.GONE
                vowelsButton?.visibility = View.VISIBLE
                nonPulmonicButton?.visibility = View.VISIBLE
                diacriticButton?.visibility = View.VISIBLE
                symbolButton?.visibility = View.VISIBLE
                pulmonicMannerButtons.forEach { it.visibility = View.VISIBLE }
                nonPulmonicMannerButtons.forEach { it.visibility = View.GONE }
                diacriticButtons.forEach { it.visibility = View.GONE }
                resetRounding(roundingButton, container, flow)
            }

            Layout.VOWEL -> {
                voicingButton?.visibility = View.GONE
                roundingButton?.visibility = View.VISIBLE
                pulmonicButton?.visibility = View.VISIBLE
                vowelsButton?.visibility = View.GONE
                nonPulmonicButton?.visibility = View.VISIBLE
                diacriticButton?.visibility = View.VISIBLE
                symbolButton?.visibility = View.VISIBLE
                pulmonicMannerButtons.forEach { it.visibility = View.GONE }
                nonPulmonicMannerButtons.forEach { it.visibility = View.GONE }
                diacriticButtons.forEach { it.visibility = View.GONE }
                resetVoicing(voicingButton, container, flow)
            }

            Layout.NONPULMONIC -> {
                voicingButton?.visibility = View.VISIBLE
                roundingButton?.visibility = View.GONE
                pulmonicButton?.visibility = View.VISIBLE
                vowelsButton?.visibility = View.VISIBLE
                nonPulmonicButton?.visibility = View.GONE
                diacriticButton?.visibility = View.VISIBLE
                symbolButton?.visibility = View.VISIBLE
                pulmonicMannerButtons.forEach { it.visibility = View.GONE }
                nonPulmonicMannerButtons.forEach { it.visibility = View.VISIBLE }
                diacriticButtons.forEach { it.visibility = View.GONE }
                resetRounding(roundingButton, container, flow)
            }

            Layout.DIACRITIC -> {
                voicingButton?.visibility = View.GONE
                roundingButton?.visibility = View.GONE
                pulmonicButton?.visibility = View.VISIBLE
                vowelsButton?.visibility = View.VISIBLE
                nonPulmonicButton?.visibility = View.VISIBLE
                diacriticButton?.visibility = View.GONE
                symbolButton?.visibility = View.VISIBLE
                pulmonicMannerButtons.forEach { it.visibility = View.GONE }
                nonPulmonicMannerButtons.forEach { it.visibility = View.GONE }
                diacriticButtons.forEach { it.visibility = View.VISIBLE }
                resetVoicing(voicingButton, container, flow)
                resetRounding(roundingButton, container, flow)
            }

            Layout.SYMBOL -> {
                voicingButton?.visibility = View.GONE
                roundingButton?.visibility = View.GONE
                pulmonicButton?.visibility = View.VISIBLE
                vowelsButton?.visibility = View.VISIBLE
                nonPulmonicButton?.visibility = View.VISIBLE
                diacriticButton?.visibility = View.VISIBLE
                symbolButton?.visibility = View.GONE
                pulmonicMannerButtons.forEach { it.visibility = View.GONE }
                nonPulmonicMannerButtons.forEach { it.visibility = View.GONE }
                diacriticButtons.forEach { it.visibility = View.GONE }
                resetVoicing(voicingButton, container, flow)
                resetRounding(roundingButton, container, flow)
            }
        }
    }

    // Handles diacritics that need an additional character to display properly on the keys, but
    // need to not be typed with that character
    private fun getOutputText(symbol: String): String {
        return when (symbol) {
            "CGJ" -> "\u034F"
            else -> symbol.replace("◌", "")
        }
    }
}