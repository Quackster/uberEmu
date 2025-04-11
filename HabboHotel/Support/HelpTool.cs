﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Data;

using Uber.Storage;
using Uber.Messages;

namespace Uber.HabboHotel.Support
{
    class HelpTool
    {
        public Dictionary<uint, HelpCategory> Categories;
        public Dictionary<uint, HelpTopic> Topics;

        public List<HelpTopic> ImportantTopics;
        public List<HelpTopic> KnownIssues;

        public HelpTool()
        {
            Categories = new Dictionary<uint, HelpCategory>();
            Topics = new Dictionary<uint, HelpTopic>();

            ImportantTopics = new List<HelpTopic>();
            KnownIssues = new List<HelpTopic>();
        }

        public void LoadCategories()
        {
            Categories.Clear();
            DataTable CategoryData = null;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                CategoryData = dbClient.ReadDataTable("SELECT * FROM help_subjects");
            }

            if (CategoryData == null)
            {
                return;
            }

            foreach (DataRow Row in CategoryData.Rows)
            {
                Categories.Add((uint)Row["id"], new HelpCategory((uint)Row["id"], (string)Row["caption"]));
            }
        }

        public HelpCategory GetCategory(uint CategoryId)
        {
            if (Categories.ContainsKey(CategoryId))
            {
                return Categories[CategoryId];
            }

            return null;
        }

        public void ClearCategories()
        {
            Categories.Clear();
        }

        public void LoadTopics()
        {
            Topics.Clear();
            DataTable TopicData = null;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                TopicData = dbClient.ReadDataTable("SELECT * FROM help_topics");
            }

            if (TopicData == null)
            {
                return;
            }

            foreach (DataRow Row in TopicData.Rows)
            {
                HelpTopic NewTopic = new HelpTopic((uint)Row["id"], (string)Row["title"], (string)Row["body"], (uint)Row["subject"]);

                Topics.Add((uint)Row["id"], NewTopic);

                int Importance = int.Parse(Row["known_issue"].ToString());

                if (Importance == 1)
                {
                    KnownIssues.Add(NewTopic);
                }
                else if (Importance == 2)
                {
                    ImportantTopics.Add(NewTopic);
                }
            }
        }

        public HelpTopic GetTopic(uint TopicId)
        {
            if (Topics.ContainsKey(TopicId))
            {
                return Topics[TopicId];
            }

            return null;            
        }

        public void ClearTopics()
        {
            Topics.Clear();

            ImportantTopics.Clear();
            KnownIssues.Clear();
        }

        public int ArticlesInCategory(uint CategoryId)
        {
            int i = 0;

            lock (Topics)
            {
                foreach (HelpTopic Topic in Topics.Values)
                {
                    if (Topic.CategoryId == CategoryId)
                    {
                        i++;
                    }
                }
            }

            return i;
        }

        public ServerMessage SerializeFrontpage()
        {
            ServerMessage Frontpage = new ServerMessage(518);
            Frontpage.AppendInt32(ImportantTopics.Count);

            lock (ImportantTopics)
            {
                foreach (HelpTopic Topic in ImportantTopics)
                {
                    Frontpage.AppendUInt(Topic.TopicId);
                    Frontpage.AppendStringWithBreak(Topic.Caption);
                }
            }

            Frontpage.AppendInt32(KnownIssues.Count);

            lock (KnownIssues)
            {
                foreach (HelpTopic Topic in KnownIssues)
                {
                    Frontpage.AppendUInt(Topic.TopicId);
                    Frontpage.AppendStringWithBreak(Topic.Caption);
                }
            }

            return Frontpage;
        }

        public ServerMessage SerializeIndex()
        {
            ServerMessage Index = new ServerMessage(519);
            Index.AppendInt32(Categories.Count);

            lock (Categories)
            {
                foreach (HelpCategory Category in Categories.Values)
                {
                    Index.AppendUInt(Category.CategoryId);
                    Index.AppendStringWithBreak(Category.Caption);
                    Index.AppendInt32(ArticlesInCategory(Category.CategoryId));
                }
            }

            return Index;
        }

        public ServerMessage SerializeTopic(HelpTopic Topic)
        {
            ServerMessage Top = new ServerMessage(520);
            Top.AppendUInt(Topic.TopicId);
            Top.AppendStringWithBreak(Topic.Body);
            return Top;
        }

        public ServerMessage SerializeSearchResults(string Query)
        {
            DataTable Results = null;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.AddParamWithValue("query", Query);
                Results = dbClient.ReadDataTable("SELECT id,title FROM help_topics WHERE title LIKE @query OR body LIKE @query LIMIT 25");
            }

            // HII[KBCan I pay to be unbanned?

            ServerMessage Search = new ServerMessage(521);

            if (Results == null)
            {
                Search.AppendBoolean(false);
                return Search;
            }

            Search.AppendInt32(Results.Rows.Count);

            foreach (DataRow Row in Results.Rows)
            {
                Search.AppendUInt((uint)Row["id"]);
                Search.AppendStringWithBreak((string)Row["title"]);
            }

            return Search;
        }

        public ServerMessage SerializeCategory(HelpCategory Category)
        {
            ServerMessage Cat = new ServerMessage(522);
            Cat.AppendUInt(Category.CategoryId);
            Cat.AppendStringWithBreak("");
            Cat.AppendInt32(ArticlesInCategory(Category.CategoryId));

            lock (Topics)
            {
                foreach (HelpTopic Topic in Topics.Values)
                {
                    if (Topic.CategoryId == Category.CategoryId)
                    {
                        Cat.AppendUInt(Topic.TopicId);
                        Cat.AppendStringWithBreak(Topic.Caption);
                    }
                }
            }

            return Cat;
        }
    }
}
