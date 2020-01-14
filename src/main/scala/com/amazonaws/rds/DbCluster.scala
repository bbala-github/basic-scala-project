package com.amazonaws.rds

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.rds.input.{DbClusterParameterGroup, DbClusterRequest, Parameter}
import com.amazonaws.regions.Regions
import com.amazonaws.services.rds.AmazonRDSClientBuilder
import com.amazonaws.services.rds.model.{CreateDBClusterParameterGroupRequest, DBClusterParameterGroupNotFoundException, DBParameterGroupNotFoundException, DescribeDBClusterParameterGroupsRequest, DescribeDBClusterParametersRequest, ModifyDBClusterParameterGroupRequest, DBCluster => RdsDbCluster, DBClusterParameterGroup => RdsDBClusterParameterGroup, Parameter => RdsParameter, Tag => RdsTag}
import com.typesafe.scalalogging.Logger

import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters._

class DbCluster(credsProvider: AWSCredentialsProvider, region: Regions) {
  private val log = Logger(classOf[DbCluster])
  private val rdsClient = AmazonRDSClientBuilder.standard.withCredentials(credsProvider).withRegion(region).build

  private def getParameters(input: DbClusterParameterGroup): List[RdsParameter] = {
    val params: ListBuffer[RdsParameter] = ListBuffer.empty
    var paginationToken: String = null
    do {
      val req = new DescribeDBClusterParametersRequest()
        .withDBClusterParameterGroupName(input.name)
        .withMarker(paginationToken)
        .withMaxRecords(100)
      val res = rdsClient.describeDBClusterParameters(req)
      paginationToken = res.getMarker
      params ++= res.getParameters.asScala
    } while (paginationToken != null)
    println(s"Params: ${params.toList}")
    params.toList
  }

  private def createDbClusterParameterGroup(input: DbClusterParameterGroup): RdsDBClusterParameterGroup = {
    val req = new CreateDBClusterParameterGroupRequest()
      .withDBClusterParameterGroupName(input.name)
      .withDBParameterGroupFamily(input.family)
      .withDescription(input.desc)
      .withTags(input.tags.map(t => new RdsTag().withKey(t.key).withValue(t.value)).asJava)
    val res = rdsClient.createDBClusterParameterGroup(req)
    validateAndModify(input)
    res
  }

  /**
   * Validate input parameters against real data and update as necessary.
   * @param input required parameter group definition.
   */
  private def validateAndModify(input: DbClusterParameterGroup) {
    val dbClusterParams = getParameters(input).map(p => p.getParameterName -> p).toMap
    val paramsToUpdate = input.params.filterNot(p => p.value.equals(dbClusterParams(p.key).getParameterValue))
    if (paramsToUpdate.nonEmpty) {
      val modifyReq = new ModifyDBClusterParameterGroupRequest()
        .withDBClusterParameterGroupName(input.name)
        .withParameters(paramsToUpdate.map(p => {
          dbClusterParams(p.key).withParameterValue(p.value).withApplyMethod(p.applyMethod)
        }).asJava)
      rdsClient.modifyDBClusterParameterGroup(modifyReq)
      log.info(s"${input.name} successfully update with updates: $paramsToUpdate")
    } else {
      log.info(s"${input.name} is up-to-date")
    }
  }

  private def isExists(input: DbClusterParameterGroup): Option[RdsDBClusterParameterGroup] = {
    val req = new DescribeDBClusterParameterGroupsRequest().withDBClusterParameterGroupName(input.name)
    val res = rdsClient.describeDBClusterParameterGroups(req)
    if (res.getDBClusterParameterGroups.isEmpty) Option.empty else Option.apply(res.getDBClusterParameterGroups.get(0))
  }

  private def createOrModifyDbClusterParameterGroup(input: DbClusterParameterGroup) {
    val req = new DescribeDBClusterParameterGroupsRequest().withDBClusterParameterGroupName(input.name)

    try {
      rdsClient.describeDBClusterParameterGroups(req)
      validateAndModify(input)
    } catch {
      case _: DBParameterGroupNotFoundException =>
        log.warn(s"DB Cluster Parameter Group for $input was not found, creating...")
        val res = createDbClusterParameterGroup(input)
        log.info(s"$res created for request $input")
    }
  }

  def create(req: DbClusterRequest) {
    createOrModifyDbClusterParameterGroup(req.dbClusterParameterGroup)
  }
}
