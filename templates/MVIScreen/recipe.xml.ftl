<?xml version="1.0"?>
<#import "root://activities/common/kotlin_macros.ftl" as kt>
<recipe>

    <instantiate from="res/layout/blank_screen.xml.ftl"
                    to="${escapeXmlAttribute(resOut)}/layout/${escapeXmlAttribute(featureName)?lower_case}_screen.xml" />

<#if includeEpoxy>
    <instantiate from="res/layout/blank_view.xml.ftl"
                    to="${escapeXmlAttribute(resOut)}/layout/${escapeXmlAttribute(featureName)?lower_case}_view.xml" />
</#if>

    <open file="${escapeXmlAttribute(resOut)}/layout/${escapeXmlAttribute(featureName)?lower_case}_screen.xml" />

    <instantiate from="src/app_package/BlankScreen.kt.ftl"
                   to="${escapeXmlAttribute(srcOut)}/${featureName?lower_case}/${featureName}Screen.kt" />

    <open file="${escapeXmlAttribute(srcOut)}/${featureName?lower_case}/${featureName}Screen.kt" />

    <instantiate from="src/app_package/BlankContract.kt.ftl"
                   to="${escapeXmlAttribute(srcOut)}/${featureName?lower_case}/${featureName}Contract.kt" />

    <open file="${escapeXmlAttribute(srcOut)}/${featureName?lower_case}/${featureName}Contract.kt" />

    <instantiate from="src/app_package/BlankPresenter.kt.ftl"
                to="${escapeXmlAttribute(srcOut)}/${featureName?lower_case}/${featureName}Presenter.kt" />

    <open file="${escapeXmlAttribute(srcOut)}/${featureName?lower_case}/${featureName}Presenter.kt" />

<#if includeEpoxy>
    <instantiate from="src/app_package/BlankController.kt.ftl"
                to="${escapeXmlAttribute(srcOut)}/${featureName?lower_case}/${featureName}Controller.kt" />

    <instantiate from="src/app_package/views/BlankView.kt.ftl"
                to="${escapeXmlAttribute(srcOut)}/${featureName?lower_case}/views/${featureName}View.kt" />
</#if>
    <instantiate from="src/app_package/_di/BlankModule.kt.ftl"
                to="${escapeXmlAttribute(srcOut)}/${featureName?lower_case}/_di/${featureName}Module.kt" />

</recipe>
