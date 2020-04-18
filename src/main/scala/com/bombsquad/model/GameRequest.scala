package com.bombsquad.model

import com.bombsquad.AppConf

case class GameRequest(rows: Int = AppConf.gameDefaultRows, cols: Int = AppConf.gameDefaultCols, bombs: Int = AppConf.gameDefaultBombs)
