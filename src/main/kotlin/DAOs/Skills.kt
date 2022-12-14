package com.theStuffGatherer.DAOs

import com.theStuffGatherer.DTOs.SkillDTO
import com.theStuffGatherer.tables.SkillsTable
import org.jetbrains.exposed.dao.ColumnWithTransform
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column

class Skills(id: EntityID<Long>): LongEntity(id) {
  companion object: LongEntityClass<Skills>(SkillsTable)

  var userId by Player referencedOn SkillsTable.userId
  var fishingSkill by SkillsTable.fishingSkill.transformSkill()
  var choppingSkill by SkillsTable.choppingSkill.transformSkill()
  var miningSkill by SkillsTable.miningSkill.transformSkill()
  var exploringSkill by SkillsTable.exploringSkill.transformSkill()

  val skills : MutableList<SkillDTO>
    get() =  mutableListOf(fishingSkill, choppingSkill, miningSkill, exploringSkill)

  fun addSkillExp(skillName: String, expAmount: Int): SkillDTO {
    val skill = skills.find { it.name == skillName }!!
    skill.exp += expAmount
    return skill
  }


  private fun Column<String>.transformSkill(): ColumnWithTransform<String, SkillDTO> {

    val transformed = this.transform(
      { tReal ->
        "${tReal.name},${tReal.level},${tReal.progress},${tReal.exp},${tReal.maxExp}"
      },
      { tColumn ->
          val properties = tColumn.split(",")
          SkillDTO(
            properties[0],
            properties[1].toInt(),
            properties[2].toDouble(),
            properties[3].toLong(),
            properties[4].toLong()
          )

      })
    return transformed
  }
}