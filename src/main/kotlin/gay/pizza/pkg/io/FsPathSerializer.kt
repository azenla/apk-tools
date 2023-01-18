package gay.pizza.pkg.io

import gay.pizza.pkg.PlatformPath
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object FsPathSerializer : KSerializer<FsPath> {
  override val descriptor: SerialDescriptor = String.serializer().descriptor

  override fun deserialize(decoder: Decoder): FsPath {
    return PlatformPath(decoder.decodeString())
  }

  override fun serialize(encoder: Encoder, value: FsPath) {
    encoder.encodeString(value.fullPathString)
  }
}
