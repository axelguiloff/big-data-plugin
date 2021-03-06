/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.step.mqtt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.streaming.common.BaseStreamStepMeta;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStreamingDialog;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

public class MQTTConsumerDialog extends BaseStreamingDialog implements StepDialogInterface {

  private static Class<?> PKG = MQTTConsumerMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  private MQTTConsumerMeta mqttMeta;
  protected Label wlConnection;
  protected TextVar wConnection;
  private Label wlTopics;
  private TableView topicsTable;
  private CTabItem wFieldsTab;
  private Composite wFieldsComp;
  private TableView fieldsTable;

  public MQTTConsumerDialog( Shell parent, Object in, TransMeta tr, String sname ) {
    super( parent, in, tr, sname );
    mqttMeta = (MQTTConsumerMeta) in;
  }

  @Override protected void getData() {
    super.getData();
    wConnection.setText( mqttMeta.getMqttServer() );
    populateTopicsData();
  }

  private void populateTopicsData() {
    List<String> topics = mqttMeta.getTopics();
    int rowIndex = 0;
    for ( String topic : topics ) {
      TableItem key = topicsTable.getTable().getItem( rowIndex++ );
      if ( topic != null ) {
        key.setText( 1, topic );
      }
    }
  }

  @Override protected String getDialogTitle() {
    return BaseMessages.getString( PKG, "MQTTConsumerDialog.Shell.Title" );
  }

  @Override protected void buildSetup( Composite wSetupComp ) {
    props.setLook( wSetupComp );
    FormLayout setupLayout = new FormLayout();
    setupLayout.marginHeight = 15;
    setupLayout.marginWidth = 15;
    wSetupComp.setLayout( setupLayout );

    wlConnection = new Label( wSetupComp, SWT.LEFT );
    props.setLook( wlConnection );
    wlConnection.setText( BaseMessages.getString( PKG, "MQTTConsumerDialog.Connection" ) );
    FormData fdlConnection = new FormData();
    fdlConnection.left = new FormAttachment( 0, 0 );
    fdlConnection.top = new FormAttachment( 0, 0 );
    fdlConnection.right = new FormAttachment( 50, 0 );
    wlConnection.setLayoutData( fdlConnection );

    wConnection = new TextVar( transMeta, wSetupComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wConnection );
    wConnection.addModifyListener( lsMod );
    FormData fdConnection = new FormData();
    fdConnection.left = new FormAttachment( 0, 0 );
    fdConnection.right = new FormAttachment( 75, 0 );
    fdConnection.top = new FormAttachment( wlConnection, 5 );
    wConnection.setLayoutData( fdConnection );

    wlTopics = new Label( wSetupComp, SWT.LEFT );
    props.setLook( wlTopics );
    wlTopics.setText( BaseMessages.getString( PKG, "MQTTConsumerDialog.Topics" ) );
    FormData fdlTopics = new FormData();
    fdlTopics.left = new FormAttachment( 0, 0 );
    fdlTopics.top = new FormAttachment( wConnection, 10 );
    fdlTopics.right = new FormAttachment( 50, 0 );
    wlTopics.setLayoutData( fdlTopics );

    buildTopicsTable( wSetupComp, wlTopics );
  }

  private void buildTopicsTable( Composite parentWidget, Control controlAbove ) {
    ColumnInfo[] columns = new ColumnInfo[]{ new ColumnInfo( BaseMessages.getString( PKG, "MQTTConsumerDialog.TopicHeading" ),
      ColumnInfo.COLUMN_TYPE_CCOMBO, new String[1], false ) };

    columns[0].setUsingVariables( true );

    int topicsCount = mqttMeta.getTopics().size();

    topicsTable = new TableView(
      transMeta,
      parentWidget,
      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI,
      columns,
      topicsCount,
      false,
      lsMod,
      props,
      false
    );

    topicsTable.setSortable( false );
    topicsTable.getTable().addListener( SWT.Resize, event -> {
      Table table = (Table) event.widget;
      table.getColumn( 1 ).setWidth( 316 );
    } );

    FormData fdData = new FormData();
    fdData.left = new FormAttachment( 0, 0 );
    fdData.top = new FormAttachment( controlAbove, 5 );
    fdData.right = new FormAttachment( 0, 337 );
    fdData.bottom = new FormAttachment( 100, 0 );

    // resize the columns to fit the data in them
    stream( topicsTable.getTable().getColumns() ).forEach( column -> {
      if ( column.getWidth() > 0 ) {
        // don't pack anything with a 0 width, it will resize it to make it visible (like the index column)
        column.setWidth( 120 );
      }
    } );

    topicsTable.setLayoutData( fdData );
  }

  @Override protected void createAdditionalTabs() {
    // Set the height so the topics table has approximately 5 rows
    shell.setMinimumSize( 527, 500 );
    buildFieldsTab();
  }

  @Override protected String[] getFieldNames() {
    return stream( fieldsTable.getTable().getItems() ).map( row -> row.getText( 2 ) ).toArray( String[]::new );
  }

  @Override protected int[] getFieldTypes() {
    return stream( fieldsTable.getTable().getItems() )
      .mapToInt( row -> ValueMetaFactory.getIdForValueMeta( row.getText( 3 )  ) ).toArray();
  }

  private void buildFieldsTab() {
    wFieldsTab = new CTabItem( wTabFolder, SWT.NONE );
    wFieldsTab.setText( BaseMessages.getString( PKG, "MQTTConsumerDialog.FieldsTab" ) );

    wFieldsComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wFieldsComp );
    FormLayout fieldsLayout = new FormLayout();
    fieldsLayout.marginHeight = 15;
    fieldsLayout.marginWidth = 15;
    wFieldsComp.setLayout( fieldsLayout );

    FormData fieldsFormData = new FormData();
    fieldsFormData.left = new FormAttachment( 0, 0 );
    fieldsFormData.top = new FormAttachment( wFieldsComp, 0 );
    fieldsFormData.right = new FormAttachment( 100, 0 );
    fieldsFormData.bottom = new FormAttachment( 100, 0 );
    wFieldsComp.setLayoutData( fieldsFormData );

    buildFieldTable( wFieldsComp, wFieldsComp );

    wFieldsComp.layout();
    wFieldsTab.setControl( wFieldsComp );
  }

  private void buildFieldTable( Composite parentWidget, Control relativePosition ) {
    ColumnInfo[] columns = getFieldColumns();

    fieldsTable = new TableView(
      transMeta,
      parentWidget,
      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI,
      columns,
      2,
      true,
      lsMod,
      props,
      false
    );

    fieldsTable.setSortable( false );
    fieldsTable.getTable().addListener( SWT.Resize, event -> {
      Table table = (Table) event.widget;
      table.getColumn( 1 ).setWidth( 147 );
      table.getColumn( 2 ).setWidth( 147 );
      table.getColumn( 3 ).setWidth( 147 );
    } );

    populateFieldData();

    FormData fdData = new FormData();
    fdData.left = new FormAttachment( 0, 0 );
    fdData.top = new FormAttachment( relativePosition, 5 );
    fdData.right = new FormAttachment( 100, 0 );

    // resize the columns to fit the data in them
    stream( fieldsTable.getTable().getColumns() ).forEach( column -> {
      if ( column.getWidth() > 0 ) {
        // don't pack anything with a 0 width, it will resize it to make it visible (like the index column)
        column.setWidth( 120 );
      }
    } );

    // don't let any rows get deleted or added (this does not affect the read-only state of the cells)
    fieldsTable.setReadonly( true );
    fieldsTable.setLayoutData( fdData );
  }

  private void populateFieldData() {
    TableItem messageItem = fieldsTable.getTable().getItem( 0 );
    messageItem.setText( 1, BaseMessages.getString( PKG, "MQTTConsumerDialog.InputName.Message" ) );
    messageItem.setText( 2, mqttMeta.getMsgOutputName() );
    messageItem.setText( 3, "String" );

    TableItem topicItem = fieldsTable.getTable().getItem( 1 );
    topicItem.setText( 1, BaseMessages.getString( PKG, "MQTTConsumerDialog.InputName.Topic" ) );
    topicItem.setText( 2, mqttMeta.getTopicOutputName() );
    topicItem.setText( 3, "String" );
  }

  private ColumnInfo[] getFieldColumns() {
    ColumnInfo referenceName = new ColumnInfo( BaseMessages.getString( PKG, "MQTTConsumerDialog.Column.Ref" ),
      ColumnInfo.COLUMN_TYPE_TEXT, false, true );

    ColumnInfo name = new ColumnInfo( BaseMessages.getString( PKG, "MQTTConsumerDialog.Column.Name" ),
      ColumnInfo.COLUMN_TYPE_TEXT, false, false );

    ColumnInfo type = new ColumnInfo( BaseMessages.getString( PKG, "MQTTConsumerDialog.Column.Type" ),
      ColumnInfo.COLUMN_TYPE_TEXT, false, true );

    return new ColumnInfo[]{ referenceName, name, type };
  }


  @Override protected void additionalOks( BaseStreamStepMeta meta ) {
    mqttMeta.setMqttServer( wConnection.getText() );
    mqttMeta.setTopics( stream( topicsTable.getTable().getItems() )
      .map( item -> item.getText( 1 ) )
      .filter( t -> !"".equals( t ) )
      .distinct()
      .collect( Collectors.toList() ) );
    mqttMeta.setMsgOutputName( fieldsTable.getTable().getItem( 0 ).getText( 2 ) );
    mqttMeta.setTopicOutputName( fieldsTable.getTable().getItem( 1 ).getText( 2 ) );
  }
}
