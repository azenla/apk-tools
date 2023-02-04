package gay.pizza.pkg.melange

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class MelangeModel {
  @Serializable
  class Scriptlets(
    @SerialName("pre-install")
    val preInstall: Trigger? = null,
    @SerialName("post-install")
    val postInstall: Trigger? = null,
    @SerialName("pre-deinstall")
    val preDeinstall: Trigger? = null,
    @SerialName("pre-upgrade")
    val preUpgrade: Trigger? = null,
    @SerialName("post-upgrade")
    val postUpgrade: Trigger? = null
  ) {
    @Serializable
    class Trigger(
      val script: String,
      val paths: List<String>
    )
  }

  @Serializable
  class PackageOption(
    @SerialName("no-provides")
    val noProvides: Boolean = false,
    @SerialName("no-depends")
    val noDepends: Boolean = false,
    @SerialName("no-commands")
    val noCommands: Boolean = false
  )

  @Serializable
  class Dependencies(
    val runtime: List<String> = emptyList(),
    val provides: List<String> = emptyList(),
    @SerialName("provider-priority")
    val providerPriority: Int = 0
  )

  @Serializable
  class Copyright(
    val paths: List<String>,
    val attestation: String,
    val license: String
  )

  @Serializable
  class Input(
    val description: String,
    val default: String,
    val required: Boolean
  )

  @Serializable
  class Needs(
    val packages: List<String> = emptyList()
  )

  @Serializable
  class PipelineAssertions(
    @SerialName("required-steps")
    val requiredSteps: Int = 0
  )

  @Serializable
  class SBOM(
    val language: String
  )

  @Serializable
  class Pipeline(
    val name: String,
    val uses: String,
    val with: Map<String, String>,
    val runs: String,
    val pipeline: List<Pipeline>,
    val inputs: Map<String, Input>,
    val needs: Needs = Needs(),
    val label: String? = null,
    @SerialName("if")
    val ifExpression: String? = null,
    val assertions: PipelineAssertions = PipelineAssertions(),
    val sbom: SBOM
  )

  @Serializable
  class Package(
    val name: String,
    val version: String,
    val epoch: Long,
    val description: String,
    val url: String,
    val commit: String,
    @SerialName("target-architecture")
    val targetArchitecture: List<String> = emptyList(),
    val copyright: List<Copyright> = emptyList(),
    val dependencies: Dependencies = Dependencies(),
    val options: PackageOption = PackageOption(),
    val scriptlets: Scriptlets? = null
  )
}
