using azure_github_ci_cd.Controllers;
using azure_github_ci_cd.Model;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Moq;
using System;
using System.Collections.Generic;
using System.Linq;
using Xunit;

namespace azure_github_ci_cd.Tests
{
    public class HomeControllerTest
    {

        public HomeControllerTest()
        {
        }


        [Fact(DisplayName = "Index should return default view")]
        public void Index_should_return_default_view()
        {
            var controller = new HomeController();
            var viewResult = (ViewResult)controller.Index();
            var viewName = viewResult.ViewName;

            Assert.True(string.IsNullOrEmpty(viewName) || viewName == "Index");
        }


        [Fact(DisplayName = "Contact should return default view")]
        public void Contact_should_return_default_view()
        {
            var controller = new HomeController();
            var viewResult = (ViewResult)controller.Contact();
            var viewName = viewResult.ViewName;

            Assert.True(string.IsNullOrEmpty(viewName) || viewName == "Contact");
        }
    }
}
