package cn.yanhu.imchat.manager

import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallbackWrapper
import com.netease.nimlib.sdk.team.TeamService
import com.netease.nimlib.sdk.team.model.Team
import com.netease.nimlib.sdk.team.model.TeamMember

/**
 * @author: zhengjun
 * created: 2024/3/28
 * desc:
 */
object ImTeamManager {

    @JvmStatic
    fun getTeamInfo(teamId: String): Team {
        return NIMClient.getService(TeamService::class.java).queryTeamBlock(teamId)
    }

    @JvmStatic
    fun getTeamMember(teamId: String, account: String): TeamMember {
        return NIMClient.getService(TeamService::class.java).queryTeamMemberBlock(teamId, account)
    }

    fun getTeamMemberInfo(teamId: String, account: String) {
        NIMClient.getService(TeamService::class.java).queryTeamMember(teamId, account)
            .setCallback(object : RequestCallbackWrapper<TeamMember>() {
                override fun onResult(code: Int, result: TeamMember?, exception: Throwable?) {

                }
            });
    }
}