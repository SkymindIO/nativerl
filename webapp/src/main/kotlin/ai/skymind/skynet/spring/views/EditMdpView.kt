package ai.skymind.skynet.spring.views

import ai.skymind.skynet.data.db.jooq.tables.records.MdpRecord
import ai.skymind.skynet.spring.services.ExecutionService
import ai.skymind.skynet.spring.views.components.ExplainedBlock
import ai.skymind.skynet.spring.views.layouts.MainLayout
import ai.skymind.skynet.spring.views.state.UserSession
import com.juicy.JuicyAceEditor
import com.juicy.mode.JuicyAceMode
import com.juicy.theme.JuicyAceTheme
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasValue
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.BeforeEvent
import com.vaadin.flow.router.HasUrlParameter
import com.vaadin.flow.router.Route
import kotlin.properties.Delegates

@Route(value = "model/mdp/edit", layout = MainLayout::class)
class EditMdpView(
        val userSession: UserSession,
        val executionService: ExecutionService
) : VerticalLayout(), HasUrlParameter<Int> {
    var rewardEditor = JuicyAceEditor()
    var importsEditor = JuicyAceEditor()
    var variablesEditor = JuicyAceEditor()
    var resetEditor = JuicyAceEditor()
    var metricsEditor = JuicyAceEditor()

    var mdp: MdpRecord? by Delegates.observable(null as MdpRecord?) { property, oldValue, newValue ->
        newValue?.let {
            // This only sets the editor value when the mdp record **itself** changes. Not when any one of its properties
            // does change
            rewardEditor.value = it.reward
            importsEditor.value = it.imports
            variablesEditor.value = it.variables
            resetEditor.value = it.reset
            metricsEditor.value = it.metrics
        }
    }

    init {
        add(H2("ExperimentName"))
        add(HorizontalLayout(
                Button("Save").apply {
                    addClickListener {
                        mdp?.store()
                        Notification.show("Saved.")
                    }
                },
                Button("Run").apply {
                    addClickListener {
                        mdp?.let{
                            executionService.runMdp(it)
                            Notification.show("Training Started.")
                            ui.get().navigate(RunListView::class.java, it.modelId)
                        }
                    }
                }
        ))


        val rewardDescription = "This is where you enter the code for your reward function. You have the following variables available: agent (your Main Agent), before and after. The variables before and after are the result of calling your getObservation function before and after your doAction function was called. You have to assign the reward to the reward variable. \n\nFor example: \n\n reward = before[0] - after[0];"
        add(createEditor("Reward Function", rewardDescription, rewardEditor){e, mdp -> mdp.reward = e.value })

        add(H2("Advanced Options"))

        val importsDescription = "If you need any additional classes in the functions you define here, you can import them here"
        add(createEditor("Imports", importsDescription, importsEditor){e, mdp -> mdp.imports = e.value })

        val variablesDescription = "If you need additional variables that are going to be available in all of the functions defined here, you can add them in this field."
        add(createEditor("Class Variables", variablesDescription, variablesEditor){e, mdp -> mdp.variables = e.value })

        val resetDescription = "If you need to do any additional setup before the simulation can be used, you can do it with this function. You have to following variables available: agent (your Main Agent)."
        add(createEditor("Reset Function", resetDescription, resetEditor){e, mdp -> mdp.reset = e.value })

        val metricsDescription = "If you want to collect any additional metrics during the training, you can do so with this function. You have to following variables available: agent (your Main Agent)."
        add(createEditor("Metrics Function", metricsDescription, metricsEditor){e, mdp -> mdp.metrics = e.value })
    }

    private fun createEditor(title: String, explanation: String, editor: JuicyAceEditor, onChange: (e: HasValue.ValueChangeEvent<String>, mdp: MdpRecord) -> Unit): Component {
        return VerticalLayout(
                H3(title),
                ExplainedBlock(
                        Span(explanation),
                        editor.apply {
                            value = ""
                            setTheme(JuicyAceTheme.eclipse)
                            setMode(JuicyAceMode.java)
                            setWidthFull()
                            height = "10em"
                            addValueChangeListener { e ->
                                mdp?.let{ onChange(e, it) }
                            }
                        }
                ).apply{
                    setWidthFull()
                }
        )
    }

    override fun setParameter(event: BeforeEvent?, modelId: Int?) {
        val mdps = userSession.findMdps(modelId!!)
        mdp = mdps?.firstOrNull() ?: userSession.newMdp(modelId)
    }


}