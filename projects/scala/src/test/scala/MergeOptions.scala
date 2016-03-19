import org.scalatest.{Matchers, FlatSpec}
import utils.Utils

class MergeOptions extends FlatSpec with Matchers {
  "Options with None values" should "merge into an empty list" in {
    Utils.mergeOptions2List(None) shouldBe Nil
    Utils.mergeOptions2List(None, None) shouldBe Nil
    Utils.mergeOptions2List(None, None, None) shouldBe Nil
  }
  "Options with None and Some values" should "merge correctly" in {
    val o1 = None
    val o2 = Some(1)
    val o3 = Some(2)
    val o4 = Some(3)
    Utils.mergeOptions2List(o1, o2, o3, o4) should contain theSameElementsAs Vector(1, 2, 3)
    Utils.mergeOptions2List(o1, o2) should contain theSameElementsAs Vector(1)
    Utils.mergeOptions2List(o2) should contain theSameElementsAs Vector(1)
    Utils.mergeOptions2List(o2, o4) should contain theSameElementsAs Vector(1, 3)
    Utils.mergeOptions2List(o2, o4, o1) should contain theSameElementsAs Vector(1, 3)
    Utils.mergeOptions2List(o2, o1, o4, o1) should contain theSameElementsAs Vector(1, 3)
  }
}
