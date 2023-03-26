package me.marvuun.logic

import dev.kord.rest.builder.message.create.MessageCreateBuilder

class MyMenu {
  private val pages = mutableListOf< MessageCreateBuilder.() -> Unit>()
  var defaultPageIndex = 0
    set(value) {
      pageIndex = value
      field = value
    }

  private var pageIndex = defaultPageIndex

  fun navigate(i: Int) {
    pageIndex += i
    pageIndex = when {
      pageIndex > pages.lastIndex -> 0
      pageIndex < 0 -> pages.lastIndex
      else -> pageIndex
    }
  }

  fun getPage() = pages[pageIndex]

  fun page(construct:  MessageCreateBuilder.() -> Unit) {
    pages.add(construct)
  }
}

suspend fun myMenu(menuBuilder: suspend MyMenu.() -> Unit): MyMenu {
  val menu = MyMenu()
  menu.menuBuilder()
  return menu
}
