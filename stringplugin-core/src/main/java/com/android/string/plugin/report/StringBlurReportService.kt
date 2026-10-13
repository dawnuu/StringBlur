package com.android.string.plugin.report

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.tooling.events.FinishEvent
import org.gradle.tooling.events.OperationCompletionListener

/** The completion-listener subscription keeps this service alive until the build has finished. */
abstract class StringBlurReportService : BuildService<StringBlurReportService.Parameters>,
    OperationCompletionListener, AutoCloseable {

    interface Parameters : BuildServiceParameters {
        val reportFile: RegularFileProperty
        val variantName: Property<String>
        val modes: Property<String>
        val bytesMode: Property<String>
    }

    private val collector = lazy {
        StringBlurReport(parameters.variantName.get(), parameters.modes.get(), parameters.bytesMode.get())
    }

    val report: StringBlurReport
        get() = collector.value

    override fun onFinish(event: FinishEvent) = Unit

    override fun close() {
        // A cached/up-to-date transform has no new visits; retain its existing report.
        if (collector.isInitialized()) {
            collector.value.writeTo(parameters.reportFile.get().asFile)
        }
    }
}
