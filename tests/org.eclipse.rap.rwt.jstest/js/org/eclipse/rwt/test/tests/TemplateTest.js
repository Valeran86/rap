/*******************************************************************************
 * Copyright (c) 2011, 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

(function(){

var TestUtil = org.eclipse.rwt.test.fixture.TestUtil;
var Template = rwt.widgets.util.Template;

rwt.qx.Class.define( "org.eclipse.rwt.test.tests.TemplateTest", {

  extend : rwt.qx.Object,

  members : {

    testCreateWithEmptyTemplate : function() {
      var cells = [];
      var template = new Template( cells );
      assertIdentical( cells, template._cells );
    },

    testGetCellCount : function() {
      var template = createTemplate( [ "text", "text", "text" ] );

      assertEquals( 3, template.getCellCount() );
    },

    testCreateContainerFailsWithoutTarget : function() {
      var template = createTemplate( [ "text", "text", "text" ] );

      try {
        template.createContainer( null );
        fail();
      } catch( ex ) {
        //expected
      }
    },

    testCreateContainerDoesNotFailWithHtmlElement : function() {
      var template = createTemplate( [ "text", "text", "text" ] );

      var container = template.createContainer( document.createElement( "div" ) );
      assertTrue( container instanceof Object );
    },

    testRenderFailsWithoutValidContainer : function() {
      var template = createTemplate( [ "text", "text", "text" ] );

      try {
        template.render( { "bounds" : [ 0, 0, 100, 100 ], "container" : {}, "item" : null } );
        fail();
      } catch( ex ) {
        // expected
      }
    },

    testRenderCreatesElements_CreateOneTextElement : function() {
      var template = createTemplate( [ "text" ] );

      var element = render( template, createGridItem( [ "foo" ] ) );

      assertEquals( 1, element.children.length );
    },

    testRenderCreatesElements_TextElementStyles : function() {
      var template = createTemplate( [ "text" ] );

      var element = render( template, createGridItem( [ "foo" ] ) );

      assertEquals( "absolute", element.children[ 0 ].style.position );
      assertEquals( "hidden", element.children[ 0 ].style.overflow );
    },

    testRenderCreatesElements_CreateOneImageElement : function() {
      var template = createTemplate( [ "image" ] );

      var element = render( template, createGridItem( [], [ "foo.jpg" ] ) );

      assertEquals( 1, element.children.length );
    },

    testRenderCreatesElements_CreateMultipleElement : function() {
      var template = createTemplate( [ "text", "text", "text" ] );

      var element = render( template, createGridItem( [ "foo", "foo", "foo" ] ) );

      assertEquals( 3, element.children.length );
    },

    testRenderCreatesElements_ReUsesElements : function() {
      var template = createTemplate( [ "text", "text", "text" ] );

      var container = createContainer( template );
      template.render( {
        "container" : container,
        "bounds" : [ 0, 0, 100, 100 ],
        "item" : createGridItem( [ "foo", "foo", "foo" ] )
      } );
      template.render( {
        "container" : container,
        "bounds" : [ 0, 0, 100, 100 ],
        "item" : createGridItem( [ "foo", "foo", "foo" ] )
      } );

      assertEquals( 3, container.element.children.length );
    },

    testRenderCreatesElements_CreateNoTextElementWithoutText : function() {
      var template = createTemplate( [ "text", "text", "text" ] );

      var element = render( template, createGridItem( [ "foo", "", "foo" ] ) );

      assertEquals( 2, element.children.length );
    },

    testRenderCellLeft_LeftIsOffset : function() {
      var template = new Template( [ { "bindingIndex" : 0, "type" : "text", "left" : 15 } ] );

      var element = render( template, createGridItem( [ "foo" ] ) );

      assertEquals( 15, getLeft( element.firstChild ) );
    },

    testGetCellLeft_LeftIsUndefined : function() {
      var template = new Template( [ {
        "bindingIndex" : 0,
        "type" : "text",
        "width" : 10,
        "right" : 15
      } ] );
      var element = render( template, createGridItem( [ "foo" ] ), [ 100, 30 ] );

      assertEquals( 75, getLeft( element.firstChild ) );
    },

    testGetCellTop_TopIsOffset : function() {
      var template = new Template( [ { "bindingIndex" : 0, "type" : "text", "top" : 12 } ] );
      var element = render( template, createGridItem( [ "foo" ] ), [ 100, 30 ] );

      assertEquals( 12, getTop( element.firstChild ) );
    },

    testGetCellTop_TopIsUndefined : function() {
      var template = new Template( [ {
        "bindingIndex" : 0,
        "type" : "text",
        "height" : 10,
        "bottom" : 15
      } ] );
      var element = render( template, createGridItem( [ "foo" ] ), [ 100, 30 ] );

      assertEquals( 5, getTop( element.firstChild ) );
    },

    testGetCellWidth_WidthIsSet : function() {
      var template = new Template( [ { "bindingIndex" : 0, "type" : "text", "width" : 17 } ] );
      var element = render( template, createGridItem( [ "foo" ] ), [ 100, 30 ] );

      assertEquals( 17, getWidth( element.firstChild ) );
    },

    testGetCellWidth_WidthIsUndefined : function() {
      var template = new Template( [ {
        "bindingIndex" : 0,
        "type" : "text",
        "left" : 10,
        "right" : 15
      } ] );
      var element = render( template, createGridItem( [ "foo" ] ), [ 100, 30 ] );

      assertEquals( 75, getWidth( element.firstChild ) );
    },

    testGetCellHeight_HeightIsSet : function() {
      var template = new Template( [ { "bindingIndex" : 0, "type" : "text", "height" : 12 } ] );
      var element = render( template, createGridItem( [ "foo" ] ), [ 100, 30 ] );

      assertEquals( 12, getHeight( element.firstChild ) );
    },

    testGetCellHeight_HeightIsUndefined : function() {
      var template = new Template( [ {
        "bindingIndex" : 0,
        "type" : "text",
        "top" : 10,
        "bottom" : 15
      } ] );
      var element = render( template, createGridItem( [ "foo" ] ), [ 100, 30 ] );

      assertEquals( 5, getHeight( element.firstChild ) );
    },

    testGetCellType : function() {
      var template = new Template( [ { "type" : "anyString" } ] );

      assertEquals( "anyString", template.getCellType( 0 ) );
    },

    testGetText_FromGridItem : function() {
      var template = new Template( [ { "bindingIndex" : 1 }, {} ] );

      render( template, createGridItem( [ "foo", "bar" ] ) );
      assertEquals( "bar", template.getText( 0 ) );
      assertEquals( "", template.getText( 1 ) );
    },

    testGetText_ForwardArgument : function() {
      var template = new Template( [ { "bindingIndex" : 0 } ] );
      var item = createGridItem( [ "foo", "bar" ] );
      var arg;
      item.getText = function() { arg = arguments; };

      render( template, item );
      template.getText( 0, 33 );

      assertEquals( [ 0, 33 ], rwt.util.Arrays.fromArguments( arg ) );
    },

    testHasText_NoBindingOrDefault : function() {
      var template = new Template( [ { } ] );
      var item = createGridItem( [ "foo", "bar" ] );
      render( template, item );

      assertFalse( template.hasText( 0 ) );
    },

    testHasText_BoundToEmptyAndNoDefault : function() {
      var template = new Template( [ { "bindingIndex" : 0 } ] );
      var item = createGridItem( [ "", "bar" ] );
      render( template, item );

      assertFalse( template.hasText( 0 ) );
    },

    testHasText_BoundToValue : function() {
      var template = new Template( [ { "bindingIndex" : 0 } ] );
      var item = createGridItem( [ "foo", "bar" ] );
      render( template, item );

      assertTrue( template.hasText( 0 ) );
    },

    testGetImage_FromGridItem : function() {
      var template = new Template( [ { "bindingIndex" : 1 }, {} ] );

      render( template, createGridItem( [], [ "foo.png", "bar.png" ] ) );

      assertEquals( "bar.png", template.getImage( 0 ) );
      assertNull( template.getImage( 1 ) );
    },

    testGetCellFont_NotBoundIsNull : function() {
      var item = createGridItem( [ "foo", "bar" ] );
      item.setCellFonts( [ "14px Arial", "14px Arial" ] );

      var template = new Template( [ {}, {} ] );
      render( template, item );

      assertNull( template.getCellFont( 0 ) );
    },

    testGetCellFont_FromGridItem : function() {
      var template = new Template( [ { "bindingIndex" : 1 }, {} ] );
      var item = createGridItem( [ "foo", "bar" ] );

      item.setCellFonts( [ null, "14px Arial" ] );
      render( template, item );

      assertEquals( "14px Arial", template.getCellFont( 0 ) );
    },

    testGetCellFont_FromGridItemDoesOverwriteDefault : function() {
      var template = new Template( [ { "bindingIndex" : 1, "font" : [ [ "Foo" ], 17, false, false ] }, {} ] );
      var item = createGridItem( [ "foo", "bar" ] );

      item.setCellFonts( [ null, "14px Arial" ] );
      render( template, item );

      assertEquals( "14px Arial", template.getCellFont( 0 ) );
    },

    testGetCellFont_FromDefaultUnbound : function() {
      var template = new Template( [ { "font" : [ [ "Arial" ], 14, false, false ] }, {} ] );

      render( template, createGridItem( [ "foo", "bar" ] ) );

      assertEquals( "14px Arial", template.getCellFont( 0 ) );
    },

    testGetCellFont_FromDefaultItemNotSet : function() {
      var template = new Template( [ { "bindingIndex" : 1, "font" : [ [ "Arial" ], 14, false, false ] }, {} ] );

      render( template, createGridItem( [ "foo", "bar" ] ) );

      assertEquals( "14px Arial", template.getCellFont( 0 ) );
    },

    testGetCellForeground_NotBoundIsNull : function() {
      var item = createGridItem( [ "foo", "bar" ] );
      item.setCellForegrounds( [ "rgb( 0, 1, 2 )", "rgb( 3, 4, 5 )" ] );

      var template = new Template( [ {}, {} ] );
      render( template, item );

      assertNull( template.getCellForeground( 0 ) );
    },

    testGetCellForeground_FromGridItem : function() {
      var template = new Template( [ { "bindingIndex" : 1 }, {} ] );
      var item = createGridItem( [ "foo", "bar" ] );

      item.setCellForegrounds( [ null, "rgb( 3, 4, 5 )" ] );
      render( template, item );

      assertEquals( "rgb( 3, 4, 5 )", template.getCellForeground( 0 ) );
    },

    testGetCellForeground_FromGridItemDoesOverwriteDefault : function() {
      var template = new Template( [ { "bindingIndex" : 1, "foreground" : [ 255, 0, 0, 255 ] } ] );
      var item = createGridItem( [ "foo", "bar" ] );

      item.setCellForegrounds( [ null, "rgb( 3, 4, 5 )" ] );
      render( template, item );

      assertEquals( "rgb( 3, 4, 5 )", template.getCellForeground( 0 ) );
    },

    testGetCellForeground_FromDefaultUnbound : function() {
      var template = new Template( [ { "foreground" : [ 255, 0, 0, 255 ] } ] );

      render( template, createGridItem( [ "foo", "bar" ] ) );

      assertEquals( "rgb(255,0,0)", template.getCellForeground( 0 ) );
    },

    testGetCellForeground_FromDefaultItemValueNotSet : function() {
      var template = new Template( [ { "bindingIndex" : 1, "foreground" : [ 255, 0, 0, 255 ] } ] );

      render( template, createGridItem( [ "foo", "bar" ] ) );

      assertEquals( "rgb(255,0,0)", template.getCellForeground( 0 ) );
    },

    testGetCellBackground_NotBoundIsNull : function() {
      var item = createGridItem( [ "foo", "bar" ] );
      item.setCellBackgrounds( [ "rgb( 0, 1, 2 )", "rgb( 3, 4, 5 )" ] );

      var template = new Template( [ {}, {} ] );
      render( template, item );

      assertNull( template.getCellBackground( 0 ) );
    },

    testGetCellBackground_FromGridItem : function() {
      var template = new Template( [ { "bindingIndex" : 1 }, {} ] );
      var item = createGridItem( [ "foo", "bar" ] );

      item.setCellBackgrounds( [ null, "rgb( 3, 4, 5 )" ] );
      render( template, item );

      assertEquals( "rgb( 3, 4, 5 )", template.getCellBackground( 0 ) );
    },

    testGetCellBackground_FromGridItemDoesOverwriteDefault : function() {
      var template = new Template( [ { "bindingIndex" : 1, "background" : [ 255, 0, 0, 255 ] } ] );
      var item = createGridItem( [ "foo", "bar" ] );

      item.setCellBackgrounds( [ null, "rgb( 3, 4, 5 )" ] );
      render( template, item );

      assertEquals( "rgb( 3, 4, 5 )", template.getCellBackground( 0 ) );
    },

    testGetCellBackground_FromDefaultUnbound : function() {
      var template = new Template( [ { "background" : [ 255, 0, 0, 255 ] } ] );

      render( template, createGridItem( [ "foo", "bar" ] ) );

      assertEquals( "rgb(255,0,0)", template.getCellBackground( 0 ) );
    },

    testGetCellBackground_FromDefaultItemValueNotSet : function() {
      var template = new Template( [ { "bindingIndex" : 1, "background" : [ 255, 0, 0, 255 ] } ] );

      render( template, createGridItem( [ "foo", "bar" ] ) );

      assertEquals( "rgb(255,0,0)", template.getCellBackground( 0 ) );
    }

  }

} );

var createTemplate = function( cellTypes ) {
  var cells = [];
  for( var i = 0; i < cellTypes.length; i++ ) {
    cells[ i ] = {
      "type" : cellTypes[ i ],
      "bindingIndex" : i
    };
  }
  return new Template( cells );
};

var createGridItem = function( texts, images ) {
  var root = new rwt.widgets.GridItem();
  root.setItemCount( 1 );
  var result = new rwt.widgets.GridItem( root, 0 );
  result.setTexts( texts );
  result.setImages( images );
  return result;
};

var render = function( template, item, dimension ) {
  var container = createContainer( template );
  template.render( {
    "container" : container,
    "item" : item,
    "bounds" : dimension ? [ 0, 0 ].concat( dimension ) : [ 0, 0, 100, 100 ]
  } );
  return container.element;
};

var createContainer = function( template ) {
  return template.createContainer( document.createElement( "div" ) );
};

var getLeft = function( element ) {
  return parseInt( element.style.left, 10 );
};

var getTop = function( element ) {
  return parseInt( element.style.top, 10 );
};

var getWidth = function( element ) {
  return parseInt( element.style.width, 10 );
};

var getHeight = function( element ) {
  return parseInt( element.style.height, 10 );
};

}());
