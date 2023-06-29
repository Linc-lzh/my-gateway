package com.gateway.rpc;

import com.gateway.annotation.RPCMethod;
import com.gateway.annotation.RPCService;

import java.util.Map;

@RPCService(scfname = "remoteServer")
public interface RemoteService {

    @RPCMethod(cmd="testCmd", needLogin=false)
    public String testMethod(Map<String, String> parms);
}
