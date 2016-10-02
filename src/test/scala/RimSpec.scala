import app.ServiceFactory
import app.ServiceFactory._
import app.restlike.common.RefProvider
import app.restlike.rim._
import im.mange.little.clock.FrozenClock
import org.joda.time.DateTime
import org.scalatest.{MustMatchers, WordSpec}

class RimSpec extends WordSpec with MustMatchers {
  //TODO: this is naughty
  ServiceFactory.systemClock.default.set(FrozenClock(new DateTime()))

  //TODO: work out what examples are missing

  private val next = "next"
  private val doing = "doing"
  private val done = "done"
  private val released = "released"
  private val workflowStates = List(next, doing, done)
  private val aka = "A"
  private val aka2 = "B"
  private val usersToAka = Map("anon" -> aka, "anon2" -> aka2)
  private val config = Config("rim", "backlog", workflowStates, released, Nil)
  private val emptyModelWithWorkflow = Model(config, usersToAka, Nil, Nil)
  private val ts = systemClock().dateTime.getMillis

  //config

  "set aka" in {
    val current = Model(Config("rim", "backlog", Nil, released, Nil), Map("anon2" -> aka2), Nil, Nil)
    val expected = current.copy(userToAka = usersToAka)
    runAndExpect("aka a", current, expected)
  }

  "set priority tags" in {
    val current = emptyModelWithWorkflow
    val expected = current.copy(config = current.config.copy ( priorityTags = List("a", "b", "c")))
    runAndExpect("tags = a b c", current, expected)
  }

  "unset priority tags" in {
    val current = emptyModelWithWorkflow
    val expected = current.copy(config = current.config.copy (priorityTags = Nil))
    runAndExpect("tags =", current, expected)
  }

  //adding

  "add issue" in {
    val current = emptyModelWithWorkflow
    val expected = current.copy(issues = List(Issue("1", "an item", None, Some(0), None, None)))
    runAndExpect("+ an item", current, expected)
  }

  "add issue (ignoring surplus noise)" in {
    val current = emptyModelWithWorkflow
    val expected = current.copy(issues = List(Issue("1", "an item", None, Some(0), None, None)))
    runAndExpect("+ an   item  ", current, expected)
  }

  "add and move forward to begin state" in {
    val current = emptyModelWithWorkflow
    val expected = current.copy(issues = List(Issue("1", "an item", None, Some(1), None, None)))
    runAndExpect("+/ an item", current, expected)
  }

  "add and move forward to second state" in {
    val current = emptyModelWithWorkflow
    val expected = current.copy(issues = List(Issue("1", "an item", None, Some(2), Some(aka), None)))
    runAndExpect("+// an item", current, expected)
  }

  "add and move forward to end state" in {
    val current = emptyModelWithWorkflow
    val expected = current.copy(issues = List(Issue("1", "an item", None, Some(config.lastWorkflowStateIncludingPre), Some(aka), None)))
    runAndExpect("+! an item", current, expected)
  }

  "add with tags" in {
    val current = emptyModelWithWorkflow
    val expected = current.copy(issues = List(Issue("1", "an item", None, Some(0), None, None, Set("tag1", "tag2"))))
    runAndExpect("+ an item : tag1 tag2", current, expected)
  }

  "strip dodgy chars from tags" in {
    val current = emptyModelWithWorkflow
    val expected = current.copy(issues = List(Issue("1", "an item", None, Some(0), None, None, Set("tag1", "tag2"))))
    runAndExpect("+ an item : :tag1 :tag2", current, expected)
  }

  "add and move forward to begin state with tags" in {
    val current = emptyModelWithWorkflow
    val expected = current.copy(issues = List(Issue("1", "an item", None, Some(1), None, None, Set("tag1", "tag2"))))
    runAndExpect("+/ an item : tag1 tag2", current, expected)
  }

  "add and move forward to end state with tags" in {
    val current = emptyModelWithWorkflow
    val expected = current.copy(issues = List(Issue("1", "an item", None, Some(config.lastWorkflowStateIncludingPre), Some(aka), None, Set("tag1", "tag2"))))
    runAndExpect("+! an item : tag1 tag2", current, expected)
  }

  //editing

  "edit issue retains by, tags and status" in {
    val issue = Issue("1", "an item", None, Some(2), Some(aka), None, Set("tag1", "tag2"))
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(Issue("1", "an item edited", None, Some(2), Some(aka), None, Set("tag1", "tag2"))))
    runAndExpect("1 = an item edited", current, expected)
  }

  "edit with tags adds tags" in {
    (pending)
    val issue = Issue("1", "an item", None, Some(2), Some(aka), None, Set("tag1", "tag2"))
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(Issue("1", "an item edited", None, Some(2), None, None, Set("tag1", "tag2", "tags3"))))
    runAndExpect("1 = an item edited : tag3", current, expected)
  }

  //moving

  "move forward one state" in {
    val issue = Issue("1", "an item", None, Some(2), None, None)
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(issue.copy(status = Some(3), by = Some(aka))))
    runAndExpect("1 /", current, expected)
  }

  //TODO: this is not completely trivial, e.g. going past done etc
//  "move forward two states" in {
//    val issue = Issue("1", "an item", Some(doing), None)
//    val current = modelWithIssue(issue)
//    val expected = current.copy(issues = List(issue.copy(status = Some(done), by = Some(aka))))
//    runAndExpect("1 //", current, expected)
//  }

  "move forward to an initial leaves disowned" in {
    val issue = Issue("1", "an item", None, Some(0), None, None)
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(issue.copy(status = Some(1))))
    runAndExpect("1 /", current, expected)
  }

  "move forward two states" in {
    (pending)
    val issue = Issue("1", "an item", None, Some(0), None, None)
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(issue.copy(status = Some(2), by = Some(aka))))
    runAndExpect("1 //", current, expected)
  }

  "move forward to end state" in {
    val issue = Issue("1", "an item", None, Some(0), None, None)
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(issue.copy(status = Some(config.lastWorkflowStateIncludingPre), by = Some(aka))))
    runAndExpect("1 /!", current, expected)
  }

  "move back a state" in {
    val issue = Issue("1", "an item", None, Some(config.lastWorkflowStateIncludingPre), None, None)
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(issue.copy(status = Some(2), by = Some(aka))))
    runAndExpect("1 .", current, expected)
  }

  "move back a state to begin state" in {
    val issue = Issue("1", "an item", None, Some(2), None, None)
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(issue.copy(status = Some(1), by = None)))
    runAndExpect("1 .", current, expected)
  }

  //TODO: by should be None
  "move back a state (into preWorkflowState)" in {
    val issue = Issue("1", "an item", None, Some(1), None, None)
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(issue.copy(status = Some(0), by = None)))
    runAndExpect("1 .", current, expected)
  }

  "move back to begin state (into preWorkflowState)" in {
//    Some(Model(Config(rim,backlog,List(next, doing, done),released,List()),Map(anon -> A, anon2 -> B),List(Issue(1,an item,None,None,None,None,Set())),List())) did not equal
    //Some(Model(Config(rim,backlog,List(next, doing, done),released,List()),Map(anon -> A, anon2 -> B),List(Issue(1,an item,None,Some(0),None,None,Set())),List()))

    val issue = Issue("1", "an item", None, Some(config.lastWorkflowStateIncludingPre), None, None)
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(issue.copy(status = Some(0), by = None)))
    runAndExpect("1 .!", current, expected)
  }

  //values

  "add a value" in {
    val issue = Issue("1", "an item", None, Some(2), None, None)
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(issue.copy(values = Set("key=value"))))
    runAndExpect("1 key=value", current, expected)
  }

  "add multiple values" in {
    val issue = Issue("1", "an item", None, Some(2), None, None)
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(issue.copy(values = Set("key1=value1", "key2=value2"))))
    runAndExpect("1 key1=value1 key2=value2", current, expected)
  }

  "add has set semantics" in {
    val issue = Issue("1", "an item", None, Some(2), None, None, values = Set("key1=value1", "key2=value2"))
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(issue.copy(values = Set("key1=value1a", "key3=value3", "key2=value2"))))
    runAndExpect("1 key1=value1a key2=value2", current, expected)
  }


  //blocking

  "block" in {
    val issue = Issue("1", "an item", None, Some(1), None, None)
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(issue.copy(blocked = Some("waiting on x"))))
    runAndExpect("1 % waiting on x", current, expected)
  }

  "unblock" in {
    val issue = Issue("1", "an item", None, Some(1), None, Some("waiting on x"))
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(issue.copy(blocked = None)))
    runAndExpect("1 %", current, expected)
  }

  //owning

  "own" in {
    val issue = Issue("1", "an item", None, Some(1), None, None)
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(issue.copy(by = Some(aka))))
    runAndExpect("1 @", current, expected)
  }

  "disown" in {
    val issue = Issue("1", "an item", None, Some(1), Some(aka), None)
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(issue.copy(by = None)))
    runAndExpect("1 @-", current, expected)
  }

  "assign" in {
    val issue = Issue("1", "an item", None, Some(1), Some(aka), None)
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(issue.copy(by = Some(aka2))))
    runAndExpect("1 @= b", current, expected)
  }

  "assign (invalid aka)" in {
    (pending) //TODO: TODO: need to start asserting the Out().messages
    val issue = Issue("1", "an item", None, Some(1), Some(aka), None)
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(issue.copy(by = Some(aka2))))
    runAndExpect("1 @= c", current, expected)
  }

  //tagging

  "tag" in {
    val issue = Issue("1", "an item", None, Some(1), None, None)
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(issue.copy(tags = Set("tag"))))
    runAndExpect("1 : tag", current, expected)
  }

  "detag" in {
    val issue = Issue("1", "an item", None, Some(1), None, None, Set("tag"))
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(issue.copy(tags = Set.empty)))
    runAndExpect("1 :- tag", current, expected)
  }

  "tag multi" in {
    val issue = Issue("1", "an item", None, Some(1), None, None)
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(issue.copy(tags = Set("tag1", "tag2", "tagN"))))
    runAndExpect("1 : tag1 tag2 tagN", current, expected)
  }

  "migrate tag" in {
    val issue = Issue("1", "an item", None, Some(1), None, None, tags = Set("tag1", "tag2", "tagN"))
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(issue.copy(tags = Set("tagX", "tag2", "tagN"))))
    runAndExpect("tag1 := tagX", current, expected)
  }

  "migrate tag in released" in {
    val issue = Issue("1", "an item", None, Some(config.lastWorkflowStateIncludingPre), None, None, tags = Set("tag1", "tag2", "tagN"))
    val current = modelWithReleasedIssue(issue)
    val expected = current.copy(released = List(current.released.head.copy(issues = List(issue.copy(tags = Set("tagX", "tag2", "tagN"))))))
    runAndExpect("tag1 := tagX", current, expected)
  }

  "delete tag" in {
    val issue = Issue("1", "an item", None, Some(1), None, None, tags = Set("tag1", "tag2", "tagN"))
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(issue.copy(tags = Set("tag1", "tagN"))))
    runAndExpect("tag2 :--", current, expected)
  }

  "delete tag in released" in {
    val issue = Issue("1", "an item", None, Some(config.lastWorkflowStateIncludingPre), None, None, tags = Set("tag1", "tag2", "tagN"))
    val current = modelWithReleasedIssue(issue)
    val expected = current.copy(released = List(current.released.head.copy(issues = List(issue.copy(tags = Set("tag1", "tagN"))))))
    runAndExpect("tag2 :--", current, expected)
  }

  //releases

  "releasing moves done issue and status to none" in {
    val issue = Issue("1", "an item", None, Some(config.lastWorkflowStateIncludingPre), None, None)
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = Nil, released = List(Release("a", List(issue.copy(status = None)), Some(systemClock().dateTime.getMillis))))
    runAndExpect("± a", current, expected)
  }

  "releasing ignores other states" in {
    (pending) // fails presumably because nothing to release
    //TODO: need modelWithIssues
    val issue = Issue("1", "an item", None, Some(2), None, None)
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(issue), released = Nil)
    runAndExpect("± a", current, expected)
  }

  "migrate legacy 'done' to 'released'" in {
    (pending) // fails presumably because nothing to release
    val issue = Issue("1", "an item", None, Some(config.lastWorkflowStateIncludingPre), None, None)
    val current = modelWithReleasedIssue(issue)
    val expected = current.copy(released = List(current.released.head.copy(issues = List(issue.copy(status = None)))))
    runAndExpect("± a", current, expected)
  }

  //show

  "show board" in {
    (pending) //TODO: need to start asserting the Out().messages
    val issue = Issue("1", "an item", None, Some(config.lastWorkflowStateIncludingPre), None, None)
    val current = modelWithIssue(issue)
    val expected = current.copy(issues = List(issue.copy(status = None, by = None)))
    runAndExpect("", current, expected)
  }

  private def runAndExpect(in: String, current: Model, expected: Model) {
    run(s"$in", current).updatedModel.mustEqual(Some(expected))
  }

  private def run(in: String, current: Model) = Commander.process(in, "anon", current, RefProvider(0), "")

  private def modelWithTags(tags: List[String]) = Model(config.copy(priorityTags = tags), usersToAka, Nil, Nil)
  private def modelWithIssue(issue: Issue) = Model(config, usersToAka, List(issue), Nil)

  private def modelWithReleasedIssue(issue: Issue) =
    Model(config, usersToAka, Nil, List(Release("release", List(issue), Some(systemClock().dateTime.getMillis))))
}
