package com.rw.article.common.sequence;

import jdk.nashorn.internal.objects.annotations.Getter;
import jdk.nashorn.internal.objects.annotations.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @author Zhou Zhong Qing
 * @Title: ${file_name}
 * @Package ${package_name}
 * @Description: ${todo}
 * @date 2018/8/118:45
 */
@Document(collection = "sequence_id")
public class SequenceId {

    @Id
    private String id;

    private long seqId;


    private String collName;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getSeqId() {
        return seqId;
    }

    public void setSeqId(long seqId) {
        this.seqId = seqId;
    }

    public String getCollName() {
        return collName;
    }

    public void setCollName(String collName) {
        this.collName = collName;
    }
}

