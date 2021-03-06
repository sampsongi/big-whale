package com.meiyouframework.bigwhale.controller.admin.cluster;

import com.alibaba.fastjson.JSON;
import com.meiyouframework.bigwhale.common.pojo.Msg;
import com.meiyouframework.bigwhale.data.domain.PageRequest;
import com.meiyouframework.bigwhale.dto.DtoClusterUser;
import com.meiyouframework.bigwhale.entity.Cluster;
import com.meiyouframework.bigwhale.entity.ClusterUser;
import com.meiyouframework.bigwhale.service.ClusterService;
import com.meiyouframework.bigwhale.service.ClusterUserService;
import com.meiyouframework.bigwhale.controller.BaseController;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/admin/cluster/cluster_user")
public class AdminClusterUserController extends BaseController {

    @Autowired
    private ClusterUserService clusterUserService;
    @Autowired
    private ClusterService clusterService;

    @RequestMapping(value = "/getpage.api", method = RequestMethod.POST)
    public Page<ClusterUser> getPage(@RequestBody DtoClusterUser req) {
        List<String> tokens = new ArrayList<>();
        if (StringUtils.isNotBlank(req.getQueue())) {
            tokens.add("queue?" + req.getQueue());
        }
        if (StringUtils.isNotBlank(req.getUid())) {
            tokens.add("uid=" + req.getUid());
        }
        if (StringUtils.isNotBlank(req.getClusterId())) {
            tokens.add("clusterId=" + req.getClusterId());
        }
        return clusterUserService.pageByQuery(new PageRequest(req.pageNo - 1, req.pageSize, StringUtils.join(tokens, ";")));
    }

    @RequestMapping(value = "/save.api", method = RequestMethod.POST)
    public Msg saveOrUpdate(@RequestBody DtoClusterUser req) {
        String msg = req.validate();
        if (msg != null) {
            return failed(msg);
        }
        if (req.getId() == null) {
            List<ClusterUser> clusterUsers = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            String [] clusterIdArr = req.getClusterId().split(",");
            for (String item : clusterIdArr) {
                ClusterUser dbClusterUser = clusterUserService.findByUidAndClusterId(req.getUid(), item);
                if (dbClusterUser != null) {
                    Cluster cluster = clusterService.findById(item);
                    errors.add(cluster.getName());
                } else {
                    dbClusterUser = new ClusterUser();
                    BeanUtils.copyProperties(req, dbClusterUser);
                    dbClusterUser.setClusterId(item);
                    clusterUsers.add(dbClusterUser);
                }
            }
            if (errors.isEmpty()) {
                Iterable<ClusterUser> clusterUserIterator = clusterUserService.saveAll(clusterUsers);
                return success(clusterUserIterator);
            } else {
                return failed("集群" + JSON.toJSON(errors) + "用户重复");
            }
        } else {
            ClusterUser dbClusterUser = clusterUserService.findById(req.getId());
            if (dbClusterUser == null)  {
                return failed();
            }
            dbClusterUser = clusterUserService.findByUidAndClusterId(req.getUid(), req.getClusterId());
            if (!dbClusterUser.getId().equals(req.getId())) {
                return failed("集群用户重复");
            }
            BeanUtils.copyProperties(req, dbClusterUser);
            dbClusterUser = clusterUserService.save(dbClusterUser);
            return success(dbClusterUser);
        }
    }

    @RequestMapping(value = "/delete.api", method = RequestMethod.POST)
    public Msg delete(@RequestParam String id) {
        clusterUserService.deleteById(id);
        return success();
    }

}
