/*
 * This file is generated by jOOQ.
 */
package ai.skymind.skynet.data.db.jooq.tables.records;


import ai.skymind.skynet.data.db.jooq.tables.Model;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record6;
import org.jooq.Row6;
import org.jooq.impl.UpdatableRecordImpl;

import javax.annotation.Generated;
import java.sql.Timestamp;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.9"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ModelRecord extends UpdatableRecordImpl<ModelRecord> implements Record6<Integer, Integer, Integer, String, String, Timestamp> {

    private static final long serialVersionUID = 1126626491;

    /**
     * Setter for <code>public.model.id</code>.
     */
    public void setId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.model.id</code>.
     */
    public Integer getId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>public.model.user_id</code>.
     */
    public void setUserId(Integer value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.model.user_id</code>.
     */
    public Integer getUserId() {
        return (Integer) get(1);
    }

    /**
     * Setter for <code>public.model.project_id</code>.
     */
    public void setProjectId(Integer value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.model.project_id</code>.
     */
    public Integer getProjectId() {
        return (Integer) get(2);
    }

    /**
     * Setter for <code>public.model.name</code>.
     */
    public void setName(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.model.name</code>.
     */
    public String getName() {
        return (String) get(3);
    }

    /**
     * Setter for <code>public.model.file_id</code>. This is the file id as returned by rescale api
     */
    public void setFileId(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>public.model.file_id</code>. This is the file id as returned by rescale api
     */
    public String getFileId() {
        return (String) get(4);
    }

    /**
     * Setter for <code>public.model.created_at</code>.
     */
    public void setCreatedAt(Timestamp value) {
        set(5, value);
    }

    /**
     * Getter for <code>public.model.created_at</code>.
     */
    public Timestamp getCreatedAt() {
        return (Timestamp) get(5);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Record1<Integer> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record6 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row6<Integer, Integer, Integer, String, String, Timestamp> fieldsRow() {
        return (Row6) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row6<Integer, Integer, Integer, String, String, Timestamp> valuesRow() {
        return (Row6) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field1() {
        return Model.MODEL.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field2() {
        return Model.MODEL.USER_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field3() {
        return Model.MODEL.PROJECT_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field4() {
        return Model.MODEL.NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field5() {
        return Model.MODEL.FILE_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Timestamp> field6() {
        return Model.MODEL.CREATED_AT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer component1() {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer component2() {
        return getUserId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer component3() {
        return getProjectId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component4() {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component5() {
        return getFileId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp component6() {
        return getCreatedAt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value1() {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value2() {
        return getUserId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value3() {
        return getProjectId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value4() {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value5() {
        return getFileId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp value6() {
        return getCreatedAt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModelRecord value1(Integer value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModelRecord value2(Integer value) {
        setUserId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModelRecord value3(Integer value) {
        setProjectId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModelRecord value4(String value) {
        setName(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModelRecord value5(String value) {
        setFileId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModelRecord value6(Timestamp value) {
        setCreatedAt(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModelRecord values(Integer value1, Integer value2, Integer value3, String value4, String value5, Timestamp value6) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached ModelRecord
     */
    public ModelRecord() {
        super(Model.MODEL);
    }

    /**
     * Create a detached, initialised ModelRecord
     */
    public ModelRecord(Integer id, Integer userId, Integer projectId, String name, String fileId, Timestamp createdAt) {
        super(Model.MODEL);

        set(0, id);
        set(1, userId);
        set(2, projectId);
        set(3, name);
        set(4, fileId);
        set(5, createdAt);
    }
}
