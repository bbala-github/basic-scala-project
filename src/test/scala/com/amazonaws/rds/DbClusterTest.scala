package com.amazonaws.rds

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.rds.input.{DbClusterParameterGroup, DbClusterRequest, Parameter, Tag}
import com.amazonaws.regions.Regions
import org.scalatest.{FunSpec, Matchers}

class DbClusterTest extends FunSpec with Matchers {
  describe("DbCluster test") {
    it("should say Hello World") {
      val dbCluster = new DbCluster(new DefaultAWSCredentialsProviderChain(), Regions.AP_SOUTH_1)
      val req = DbClusterRequest(DbClusterParameterGroup("h20", "aurora5.6", "test-foo",
        List(Parameter("server_audit_events", "CONNECT,QUERY,QUERY_DCL,QUERY_DDL,QUERY_DML,TABLE", "immediate")),
        List(Tag("t1", "v1"))))
      dbCluster.create(req)
    }
  }
}
