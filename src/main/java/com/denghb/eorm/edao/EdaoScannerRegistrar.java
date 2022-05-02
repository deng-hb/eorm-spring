
package com.denghb.eorm.edao;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

import com.denghb.eorm.Edao;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;


public class EdaoScannerRegistrar implements BeanDefinitionRegistryPostProcessor {

    private String basePackage;

    public EdaoScannerRegistrar() {

    }

    public EdaoScannerRegistrar(String basePackage) {
        this.basePackage = basePackage;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }


    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
        Assert.notNull(this.basePackage, "Property 'basePackage' is required");
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry, false) {

            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                AnnotationMetadata metadata = beanDefinition.getMetadata();
                // extend Edao
                return metadata.isInterface() && metadata.isIndependent()
                
                        && Arrays.stream(metadata.getInterfaceNames()).anyMatch(e -> e.equals(Edao.class.getName()));
            }

            @Override
            protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
                Set<BeanDefinitionHolder> beanDefinitionHolders = super.doScan(basePackages);
                for (BeanDefinitionHolder beanDefinitionHolder : beanDefinitionHolders) {
                    GenericBeanDefinition beanDefinition = (GenericBeanDefinition) beanDefinitionHolder.getBeanDefinition();
                    // 将bean的真实类型改变为FactoryBean
                    beanDefinition.getConstructorArgumentValues().
                            addGenericArgumentValue(Objects.requireNonNull(beanDefinition.getBeanClassName()));
                    beanDefinition.setBeanClass(EdaoProxyFactoryBean.class);
                    beanDefinition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);
                }
                return beanDefinitionHolders;
            }
        };
        scanner.addIncludeFilter(new TypeFilter() {
            @Override
            public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
                return true;
            }
        });
        scanner.scan(StringUtils.tokenizeToStringArray(this.basePackage, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));

    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        // do something
    }
}
