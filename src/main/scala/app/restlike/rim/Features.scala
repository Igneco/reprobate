package app.restlike.rim

//NEXT:
//colourise the statuses
//consider and nice 4 blue for what youve just changed
//and then you traffic lights for status, light blue for
//think about limiting WIP
//ultimately rim : [tag] might want to be sorted by status and not broken down by status
//consider a colour blob next to the status (like a rag status), could be round or reverse video

///pointy head reporting
//consider * for collapse non blessed tags

//show the board when:
//'rim =' and issue is on the board
//'rim @=' and issue is on the board
//'rim [ref] :' and issue is on the board

//in "rim :"
//consider highlighting tags that are only in released, might indicate dead ... but oy probably wouldnt delete

//colourise help

//collect stats on command usage

//annoying issues:
//rim = should parse tags

//thoughts:
// ..... how about . instead of : for less interesting tags, or even :; (winky tags)
// ..... can/should we lose some the extra : on id's and labels within rim, make it a bit cleaner
//TIP: http://www.chriswrites.com/how-to-type-common-symbols-and-special-characters-in-os-x/
//rim ± = option shift equals .. iteration/management summary .. or use pointy hat symbol ^
//show tags by most recent etc (maybe)

//tags:
//Franck: tag many: `rim ref1 ref2 … refN : foo bar baz`
//tags should be [a-z0-9\-]

//operations to support on many:
//rim 1 2 N .
//rim 1 2 N :
//rim 1 2 N :-

//more view options:
//rim / - show begin
//rim // - show nth state
//rim ! - show end state

//releases:
//store when we released .. useful for really simple tracking

//colouring: (orange = updated, cyan = me, ? = context)
//colorise what changed .. (could be property specific)
//when doing 'rim @' consider highlight what exactly is being done

//gaps:
//properly support multiple / in /// and +///
//properly support multiple . in ...

//dates: (not yet)
//store when released (eek, data change - so make it an option)
//show how long things have been in certain states
//show stats about akas ... entries, last used etc (top 5)
//show how long since aka X updated rim

//query:
//rim . foo => should maybe search like ? does, but just for the backlog for foo ...
//or maybe not because 'and' might cover it ... although how do you search for no status

//???:
//help should have an 'issues' section for working with multiples on =, : etc
//might be nice to have rim audit (or track) and see the last x items from the history
//how do we handle rim releases getting too long?

//audit stuff
//might be good to capture who added the issue
//might be good to capture who last updated the issue
//actually if we just store the updates by id then we will get that for free
//store only things that result in a change
//rim [ref] history

//FUTURE:
//- private rims
//- grim
//- spartan bubble ui
//- hosted

//SOMEDAY/MAYBE:
//split and merge {}

//think about:
//would be nice to have a symbol for release ... it could be: ±
//so then show = 'rim ±' or _ .. as in draw a line under it
//so then create = 'rim ± [name]'

//TODO: handle corrupted rim.json
//TODO: protect against empty value
//TODO: discover common keys and present them when updating
//TODO: be careful with aka .. they need to be unique
//TODO: on update, don't show self in list of others and don't show anything if others are empty
//http://stackoverflow.com/questions/287871/print-in-terminal-with-colors-using-python?rq=1
//http://apple.stackexchange.com/questions/74777/echo-color-coding-stopped-working-in-mountain-lion
//http://unix.stackexchange.com/questions/43408/printing-colored-text-using-echo
//e.g. printf '%s \e[0;31m%s\e[0m %s\n' 'Some text' 'in color' 'no more color'
//  def red(value: String) = s"\e[1;31m $value \e[0m"

//TODO: (maybe) support curl
//#MESSAGE="(Foo) Deployed ${VERSION} to ${MACHINE_NAME}"
//#curl --connect-timeout 15 -H "Content-Type: application/json" -d "{\"message\":\"${MESSAGE}\"}" http://localhost:8765/broadcast
//#wget --timeout=15 --no-proxy -O- --post-data="{\"message\":\"${MESSAGE}\"}" --header=Content-Type:application/json "http://localhost:8765/broadcast"
