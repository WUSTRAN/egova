package com.egova.associative;


import com.egova.utils.EntityAnnotationUtils;
import com.flagwind.application.Application;
import com.flagwind.commons.StringUtils;
import com.flagwind.lang.ExtensibleObject;
import com.flagwind.reflect.EntityTypeHolder;
import com.flagwind.reflect.entities.EntityField;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.List;
import java.util.Set;

/**
 * json序列化联想动作执行器
 */
public class AssociativeExecutor {

    private static Log LOG = LogFactory.getLog(AssociativeExecutor.class);


    private static String getExpressionValue(String expression, ExtensibleObject obj, EntityField field) {
        StandardEvaluationContext ctx = new StandardEvaluationContext();
        ctx.setVariable("object", obj);
        ctx.setVariable("field", field);
        ctx.setRootObject(field);
        // 创建ExpressionParser解析表达式
        ExpressionParser parser = new SpelExpressionParser();
        // 表达式放置
        Expression exp = parser.parseExpression(expression, new ParserContext() {

            @Override
            public boolean isTemplate() {
                return true;
            }

            @Override
            public String getExpressionPrefix() {
                return "#{";
            }

            @Override
            public String getExpressionSuffix() {
                return "}";
            }
        });
        Object v = exp.getValue(ctx);
        return v == null ? field.getName() + "Text" : v.toString();
    }

    private static String getProviderName(Associative associative) {
        if (StringUtils.isEmpty(associative.providerName())) {
            return Void.class != associative.providerClass() ? associative.providerClass().getName():"";
        }
        return associative.providerName();
    }

    private static AssociativeProvider getProvider(Associative associative){
        return Application.resolve(getProviderName(associative));
    }

    private static void setAssociativeField(Associative associative, ExtensibleObject obj, EntityField field) {


        String newFieldName = StringUtils.isBlank(associative.name()) ? field.getName() + "Text" : associative.name();
        if (newFieldName.contains("%s")) {
            newFieldName = String.format(newFieldName, field.getName());
        }
        if (newFieldName.contains("${")) {
            newFieldName = newFieldName.replace("${", "#{");
        }
        if (newFieldName.contains("#{")) {
            newFieldName = getExpressionValue(newFieldName, obj, field);
        }
        if (obj.contains(newFieldName)) {
            return;
        }
        AssociativeProvider provider = getProvider(associative);
        if (provider == null) {
            LOG.error(String.format("没有找到字段%s上的联想注解的AssociativeProvider", getProviderName(associative)));
            return;
        }
        try {
            Object v = field.getValue(obj, null);
            AssociativeEntry entry = new AssociativeEntry(newFieldName, getProviderName(associative), associative.extras());

            entry.execute(obj, v);
        } catch (Exception ex) {
            LOG.error(String.format("%s对象json 联想序列化%s字段时异常", obj.getClass().getSimpleName(), field.getName()), ex);
        }

    }

    public static Object getAssociativeValue(EntityField field, Object obj) {
        Object v;
        try {
            v = field.getValue(obj, null);
        } catch (Exception ex) {
            LOG.error(String.format("%s对象json 联想序列化%s字段时异常", obj.getClass().getSimpleName(), field.getName()), ex);
            return null;
        }

        Set<Associative> associatives = EntityAnnotationUtils.getMergedRepeatableAnnotations(field, Associative.class);


        for (Associative associative : associatives) {

            AssociativeProvider provider = getProvider(associative);
            if (provider == null) {
                LOG.error(String.format("没有找到名为%s的AssociativeProvider对象", getProviderName(associative)));
                return null;
            }
            AssociativeEntry entry = new AssociativeEntry(null, getProviderName(associative), associative.extras());
            return entry.getAssociateValue(v);
        }
        return v;
    }

    public static void execute(ExtensibleObject obj) {
        List<EntityField> fields = EntityTypeHolder.getFields(obj.getClass());
        for (EntityField field : fields) {
            try {
                Set<Associative> associatives = EntityAnnotationUtils.getMergedRepeatableAnnotations(field, Associative.class);

                for (Associative associative : associatives) {
                    setAssociativeField(associative, obj, field);
                }
            } catch (Exception ex) {
                LOG.error(String.format("分析%s对象的%s联想属性出现异常", obj.getClass().getSimpleName(), field.getName()));
                throw ex;
            }
        }
    }
}
