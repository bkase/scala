package ch.epfl.lamp.llvm

object Constant {
  implicit def boolconst(v: Boolean) = {
    if (v) CTrue else CFalse
  }
  implicit def intconst(i: Int) = new CInt(LMInt.i32,i)
  implicit def byteconst(b: Byte) = new CInt(LMInt.i8,b)
  implicit def longconst(l: Long) = new CInt(LMInt.i64,l)
  implicit def shortconst(s: Short) = new CInt(LMInt.i16,s)
  implicit def floatconst(f: Float) = new CFloat(f)
  implicit def doubleconst(d: Double) = new CDouble(d)
  implicit def globalconst(g: LMGlobalVariable[_<:ConcreteType]) = new CGlobalAddress(g)
  implicit def funconst(f: LMFunction) = new CFunctionAddress(f)
}
sealed abstract class Constant[+T <: ConcreteType] extends LMValue[T]
case object CTrue extends Constant[LMInt] {
  val tpe = LMInt.i1
  def rep = "true"
}
case object CFalse extends Constant[LMInt] {
  val tpe = LMInt.i1
  def rep = "false"
}
class CInt(val tpe: LMInt, v: BigInt) extends Constant[LMInt] {
  def rep = v.toString
}
class CFloat(f: Float) extends Constant[LMFloat.type] {
  val tpe = LMFloat
  def rep = "0x"+java.lang.Long.toHexString(java.lang.Double.doubleToRawLongBits(f))
}
class CDouble(d: Double) extends Constant[LMDouble.type] {
  val tpe = LMDouble
  def rep = "0x"+java.lang.Long.toHexString(java.lang.Double.doubleToRawLongBits(d))
}
class CStruct(values: Seq[Constant[_<:ConcreteType]]) extends Constant[LMStructure] {
  lazy val tpe = new LMStructure(values.map(_.tpe))
  def rep = values.map(_.tperep).mkString("{ ",", "," }")
}
class CUnion(val tpe: LMUnion, value: Constant[_<:ConcreteType]) extends Constant[LMUnion] {
  def rep = "{ "+value.tperep+" }"
}
class CArray[T <: ConcreteType](etpe: T, values: Seq[Constant[T]]) extends Constant[LMArray] {
  lazy val tpe = new LMArray(values.length, etpe)
  def rep = values.map(_.tperep).mkString("[ ",", "," ]")
}
class CVector[T <: LMPrimitiveType with ConcreteType](etpe: T, values: Seq[Constant[T]]) extends Constant[LMVector] {
  lazy val tpe = new LMVector(values.length, etpe)
  def rep = values.map(_.tperep).mkString("< ",", "," >")
}
class CNull(val tpe: LMPointer) extends Constant[LMPointer] {
  def rep = "null"
}
class CUndef[T <: ConcreteType](val tpe: T) extends Constant[T] {
  def rep = "undef"
}
class CZeroInit[T <: ConcreteType](val tpe: T) extends Constant[T] {
  def rep = "zeroinitializer"
}
class CGlobalAddress(global: LMGlobalVariable[_<:ConcreteType]) extends Constant[LMPointer] {
  def tpe = global.tpe.pointer
  def rep = "@\""+global.name+"\""
}
class CFunctionAddress(func: LMFunction) extends Constant[LMPointer] {
  def tpe = func.tpe.pointer
  def rep = "@\""+func.name+"\""
}
class Ctrunc(v: Constant[LMInt], val tpe: LMInt) extends Constant[LMInt] {
  require(v.tpe.bits > tpe.bits )
  def rep = "trunc ("+v.tperep+" to "+tpe.rep+")"
}
class Czext(v: Constant[LMInt], val tpe: LMInt) extends Constant[LMInt] {
  require(v.tpe.bits <= tpe.bits)
  def rep = "zext ("+v.tperep+" to "+tpe.rep+")"
}
class Csext(v: Constant[LMInt], val tpe: LMInt) extends Constant[LMInt] {
  require(v.tpe.bits <= tpe.bits)
  def rep = "sext ("+v.tperep+" to "+tpe.rep+")"
}
class Cfptrunc[T <: LMFloatingPointType with ConcreteType](v: Constant[_ <: LMFloatingPointType with ConcreteType], val tpe: T) extends Constant[T] {
  require(v.tpe.bits > tpe.bits)
  def rep = "fptrunc ("+v.tperep+" to "+tpe.rep+")"
}
class Cfpext[T <: LMFloatingPointType with ConcreteType](v: Constant[_ <: LMFloatingPointType with ConcreteType], val tpe: T) extends Constant[T] {
  require(v.tpe.bits <= tpe.bits)
  def rep = "fpext ("+v.tperep+" to "+tpe.rep+")"
}
class Cfptoui(v: Constant[_ <: LMFloatingPointType with ConcreteType], val tpe: LMInt) extends Constant[LMInt] {
  def rep = "fptoui ("+v.tperep+" to "+tpe.rep+")"
}
class Cfptosi(v: Constant[_ <: LMFloatingPointType with ConcreteType], val tpe: LMInt) extends Constant[LMInt] {
  def rep = "fptosi ("+v.tperep+" to "+tpe.rep+")"
}
class Cuitofp[T <: LMFloatingPointType with ConcreteType](v: Constant[LMInt], val tpe: T) extends Constant[T] {
  def rep = "uitofp ("+v.tperep+" to "+tpe.rep+")"
}
class Csitofp[T <: LMFloatingPointType with ConcreteType](v: Constant[LMInt], val tpe: T) extends Constant[T] {
  def rep = "sitofp ("+v.tperep+" to "+tpe.rep+")"
}
class Cptrtoint(v: Constant[LMPointer], val tpe: LMInt) extends Constant[LMInt] {
  def rep = "ptrtoint ("+v.tperep+" to "+tpe.rep+")"
}
class Cinttoptr(v: Constant[LMInt], val tpe: LMPointer) extends Constant[LMPointer] {
  def rep = "inttoptr ("+v.tperep+" to "+tpe.rep+")"
}
class Cbitcast[T <: ConcreteType](v: Constant[ConcreteType], val tpe: T) extends Constant[T] {
  def rep = "bitcast ("+v.tperep+" to "+tpe.rep+")"
}
class Cgetelementptr[T <: ConcreteType](p: Constant[LMPointer], val indexes: Seq[Constant[LMInt]], val tpe: T) extends Constant[T] {
  def rep = "getelementptr ("+p.tperep+", "+indexes.map(_.tperep).mkString(", ")+")"
}
class Cgetelementptr_inbounds[T <: ConcreteType](p: Constant[LMPointer], val indexes: Seq[Constant[LMInt]], val tpe: T) extends Constant[T] {
  def rep = "getelementptr inbounds ("+p.tperep+", "+indexes.map(_.tperep).mkString(", ")+")"
}
class Cselect[T <: ConcreteType](v: Constant[LMInt], val tpe: T, v1: Constant[T], v2: Constant[T]) extends Constant[T] {
  def rep = "select ("+v.tperep+", "+v1.tperep+", "+v2.tperep+")"
}
class Cicmp(op: ICond, v1: Constant[LMInt], v2: Constant[LMInt]) extends Constant[LMInt] {
  def tpe = LMInt.i1
  def rep = "icmp "+op.name+" ("+v1.tperep+", "+v2.tperep+")"
}
class Cfcmp[T <: LMFloatingPointType with ConcreteType](op: FCond, v1: Constant[T], v2: Constant[T]) extends Constant[LMInt] {
  def tpe = LMInt.i1
  def rep = "fcmp "+op.name+" ("+v1.tperep+", "+v2.tperep+")"
}
class Cextractelement[T <: LMPrimitiveType with ConcreteType](v: Constant[LMVector], index: Constant[LMInt], val tpe: T) extends Constant[T] {
  require(v.tpe.elementtype == tpe)
  def rep = "extractelement ("+v.tperep+", "+index.tperep+")"
}
class Cinsertelement[T <: LMPrimitiveType with ConcreteType](v: Constant[LMVector], elt: Constant[T], index: Constant[LMInt]) extends Constant[LMVector] {
  def tpe = v.tpe
  def rep = "insertelement ("+v.tperep+", "+elt.tperep+", "+index.tperep+")"
}
class Cshufflevector[T <: LMPrimitiveType with ConcreteType](v1: Constant[LMVector], v2: Constant[LMVector], mask: Constant[LMVector]) extends Constant[LMVector] {
  require(v1.tpe == v2.tpe)
  require(mask.tpe.elementtype.isInstanceOf[LMInt])
  def tpe = new LMVector(mask.tpe.n, v1.tpe.elementtype)
  def rep = "shufflevector ("+v1.tperep+", "+v2.tperep+", "+mask.tperep+")"
}
class Cextractvalue[T <: ConcreteType](v: Constant[_ <: LMAggregateType with ConcreteType], indexes: Seq[Constant[LMInt]], val tpe: T) extends Constant[T] {
  def rep = "extractvalue ("+v.tperep+", "+indexes.map(_.tperep).mkString(", ")+")"
}
class Cinsertvalue[S <: LMAggregateType with ConcreteType, T <: ConcreteType](v: Constant[S], elt: Constant[T], indexes: Seq[Constant[LMInt]]) extends Constant[S] {
  def tpe = v.tpe
  def rep = "insertvalue ("+v.tperep+", "+elt.tperep+", "+indexes.map(_.tperep).mkString(", ")+")"
}
class Cadd(v1: Constant[LMInt], v2: Constant[LMInt]) extends Constant[LMInt] {
  require(v1.tpe == v2.tpe)
  def tpe = v1.tpe
  def rep = "add ("+v1.tperep+", "+v2.tperep+")"
}
class Cfadd[T <: LMFloatingPointType with ConcreteType](v1: Constant[T], v2: Constant[T]) extends Constant[T] {
  def tpe = v1.tpe
  def rep = "fadd ("+v1.tperep+", "+v2.tperep+")"
}
class Csub(v1: Constant[LMInt], v2: Constant[LMInt]) extends Constant[LMInt] {
  require(v1.tpe == v2.tpe)
  def tpe = v1.tpe
  def rep = "sub ("+v1.tperep+", "+v2.tperep+")"
}
class Cfsub[T <: LMFloatingPointType with ConcreteType](v1: Constant[T], v2: Constant[T]) extends Constant[T] {
  def tpe = v1.tpe
  def rep = "fsub ("+v1.tperep+", "+v2.tperep+")"
}
class Cmul(v1: Constant[LMInt], v2: Constant[LMInt]) extends Constant[LMInt] {
  require(v1.tpe == v2.tpe)
  def tpe = v1.tpe
  def rep = "mul ("+v1.tperep+", "+v2.tperep+")"
}
class Cfmul[T <: LMFloatingPointType with ConcreteType](v1: Constant[T], v2: Constant[T]) extends Constant[T] {
  def tpe = v1.tpe
  def rep = "fmul ("+v1.tperep+", "+v2.tperep+")"
}
class Cudiv(v1: Constant[LMInt], v2: Constant[LMInt]) extends Constant[LMInt] {
  require(v1.tpe == v2.tpe)
  def tpe = v1.tpe
  def rep = "udiv ("+v1.tperep+", "+v2.tperep+")"
}
class Csdiv(v1: Constant[LMInt], v2: Constant[LMInt]) extends Constant[LMInt] {
  require(v1.tpe == v2.tpe)
  def tpe = v1.tpe
  def rep = "sdiv ("+v1.tperep+", "+v2.tperep+")"
}
class Cfdiv[T <: LMFloatingPointType with ConcreteType](v1: Constant[T], v2: Constant[T]) extends Constant[T] {
  def tpe = v1.tpe
  def rep = "fdiv ("+v1.tperep+", "+v2.tperep+")"
}
class Curem(v1: Constant[LMInt], v2: Constant[LMInt]) extends Constant[LMInt] {
  require(v1.tpe == v2.tpe)
  def tpe = v1.tpe
  def rep = "urem ("+v1.tperep+", "+v2.tperep+")"
}
class Csrem(v1: Constant[LMInt], v2: Constant[LMInt]) extends Constant[LMInt] {
  require(v1.tpe == v2.tpe)
  def tpe = v1.tpe
  def rep = "srem ("+v1.tperep+", "+v2.tperep+")"
}
class Cfrem[T <: LMFloatingPointType with ConcreteType](v1: Constant[T], v2: Constant[T]) extends Constant[T] {
  def tpe = v1.tpe
  def rep = "frem ("+v1.tperep+", "+v2.tperep+")"
}
class Cshl(v1: Constant[LMInt], v2: Constant[LMInt]) extends Constant[LMInt] {
  require(v1.tpe == v2.tpe)
  def tpe = v1.tpe
  def rep = "shl ("+v1.tperep+", "+v2.tperep+")"
}
class Clshr(v1: Constant[LMInt], v2: Constant[LMInt]) extends Constant[LMInt] {
  require(v1.tpe == v2.tpe)
  def tpe = v1.tpe
  def rep = "lshr ("+v1.tperep+", "+v2.tperep+")"
}
class Cashr(v1: Constant[LMInt], v2: Constant[LMInt]) extends Constant[LMInt] {
  require(v1.tpe == v2.tpe)
  def tpe = v1.tpe
  def rep = "ashr ("+v1.tperep+", "+v2.tperep+")"
}
class Cand(v1: Constant[LMInt], v2: Constant[LMInt]) extends Constant[LMInt] {
  require(v1.tpe == v2.tpe)
  def tpe = v1.tpe
  def rep = "and ("+v1.tperep+", "+v2.tperep+")"
}
class Cor(v1: Constant[LMInt], v2: Constant[LMInt]) extends Constant[LMInt] {
  require(v1.tpe == v2.tpe)
  def tpe = v1.tpe
  def rep = "or ("+v1.tperep+", "+v2.tperep+")"
}
class Cxor(v1: Constant[LMInt], v2: Constant[LMInt]) extends Constant[LMInt] {
  require(v1.tpe == v2.tpe)
  def tpe = v1.tpe
  def rep = "xor ("+v1.tperep+", "+v2.tperep+")"
}
