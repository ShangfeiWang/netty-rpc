import com.wsf.netty.rpc.common.config.zookeeper.CuratorClientConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

/**
 * @author wsf
 * @since 20220526
 */
@Slf4j
public class TestCurator {

    @Test
    public void test1() throws Exception {
        CuratorClientConfig curatorClientConfig = new CuratorClientConfig("127.0.0.1:2181");

        // 创建瞬时节点
        curatorClientConfig.createPathNodeData("/wsf/test1", "hello".getBytes(StandardCharsets.UTF_8));
        // 创建持久节点
        curatorClientConfig.createPersistentPathNodeData("/wsf/test2", "hello".getBytes(StandardCharsets.UTF_8));

        curatorClientConfig.close();
    }

    @Test
    public void testDeletePath() throws Exception {
        CuratorClientConfig curatorClientConfig = new CuratorClientConfig("127.0.0.1:2181");
        curatorClientConfig.deletePath("/wsf/test2");
    }

    @Test
    public void testWatchNode() throws Exception {
        String path = "/wsf/test2";
        CuratorClientConfig curatorClientConfig = new CuratorClientConfig("127.0.0.1:2181");
        curatorClientConfig.createPersistentPathNodeData(path, "hello".getBytes(StandardCharsets.UTF_8));
        byte[] nodeData = curatorClientConfig.getNodeData(path);
        String value = new String(nodeData);
        System.out.println("value:" + value);
        curatorClientConfig.deletePath(path);
    }

}
