package com.amazonaws.rds.input

case class Tag(key: String, value: String)

case class Parameter(key: String, value: String, applyMethod: String)

case class DbClusterParameterGroup(name: String, family: String, desc: String, params: List[Parameter], tags: List[Tag])

case class DbClusterRequest(dbClusterParameterGroup: DbClusterParameterGroup)
