/*
 * This file is generated by jOOQ.
 */
package ai.skymind.skynet.data.db.jooq.tables;


import ai.skymind.skynet.data.db.jooq.Indexes;
import ai.skymind.skynet.data.db.jooq.Keys;
import ai.skymind.skynet.data.db.jooq.Public;
import ai.skymind.skynet.data.db.jooq.tables.records.ModelRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;

import javax.annotation.Generated;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;


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
public class Model extends TableImpl<ModelRecord> {

    private static final long serialVersionUID = 1408191394;

    /**
     * The reference instance of <code>public.model</code>
     */
    public static final Model MODEL = new Model();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<ModelRecord> getRecordType() {
        return ModelRecord.class;
    }

    /**
     * The column <code>public.model.id</code>.
     */
    public final TableField<ModelRecord, Integer> ID = createField("id", org.jooq.impl.SQLDataType.INTEGER.nullable(false).defaultValue(org.jooq.impl.DSL.field("nextval('model_id_seq'::regclass)", org.jooq.impl.SQLDataType.INTEGER)), this, "");

    /**
     * The column <code>public.model.user_id</code>.
     */
    public final TableField<ModelRecord, Integer> USER_ID = createField("user_id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>public.model.project_id</code>.
     */
    public final TableField<ModelRecord, Integer> PROJECT_ID = createField("project_id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>public.model.name</code>.
     */
    public final TableField<ModelRecord, String> NAME = createField("name", org.jooq.impl.SQLDataType.VARCHAR.nullable(false), this, "");

    /**
     * The column <code>public.model.file_id</code>. This is the file id as returned by rescale api
     */
    public final TableField<ModelRecord, String> FILE_ID = createField("file_id", org.jooq.impl.SQLDataType.VARCHAR.nullable(false), this, "This is the file id as returned by rescale api");

    /**
     * The column <code>public.model.created_at</code>.
     */
    public final TableField<ModelRecord, Timestamp> CREATED_AT = createField("created_at", org.jooq.impl.SQLDataType.TIMESTAMP.nullable(false).defaultValue(org.jooq.impl.DSL.field("now()", org.jooq.impl.SQLDataType.TIMESTAMP)), this, "");

    /**
     * The column <code>public.model.time_unit</code>.
     */
    public final TableField<ModelRecord, String> TIME_UNIT = createField("time_unit", org.jooq.impl.SQLDataType.VARCHAR.nullable(false).defaultValue(org.jooq.impl.DSL.field("'minutes'::character varying", org.jooq.impl.SQLDataType.VARCHAR)), this, "");

    /**
     * The column <code>public.model.step_size</code>.
     */
    public final TableField<ModelRecord, Integer> STEP_SIZE = createField("step_size", org.jooq.impl.SQLDataType.INTEGER.nullable(false).defaultValue(org.jooq.impl.DSL.field("1", org.jooq.impl.SQLDataType.INTEGER)), this, "");

    /**
     * Create a <code>public.model</code> table reference
     */
    public Model() {
        this(DSL.name("model"), null);
    }

    /**
     * Create an aliased <code>public.model</code> table reference
     */
    public Model(String alias) {
        this(DSL.name(alias), MODEL);
    }

    /**
     * Create an aliased <code>public.model</code> table reference
     */
    public Model(Name alias) {
        this(alias, MODEL);
    }

    private Model(Name alias, Table<ModelRecord> aliased) {
        this(alias, aliased, null);
    }

    private Model(Name alias, Table<ModelRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> Model(Table<O> child, ForeignKey<O, ModelRecord> key) {
        super(child, key, MODEL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.MODEL_PKEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity<ModelRecord, Integer> getIdentity() {
        return Keys.IDENTITY_MODEL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<ModelRecord> getPrimaryKey() {
        return Keys.MODEL_PKEY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<ModelRecord>> getKeys() {
        return Arrays.<UniqueKey<ModelRecord>>asList(Keys.MODEL_PKEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ForeignKey<ModelRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<ModelRecord, ?>>asList(Keys.MODEL__MODEL_OWNER, Keys.MODEL__MODEL_PROJECT);
    }

    public User user() {
        return new User(this, Keys.MODEL__MODEL_OWNER);
    }

    public Project project() {
        return new Project(this, Keys.MODEL__MODEL_PROJECT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model as(String alias) {
        return new Model(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model as(Name alias) {
        return new Model(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Model rename(String name) {
        return new Model(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Model rename(Name name) {
        return new Model(name, null);
    }
}
